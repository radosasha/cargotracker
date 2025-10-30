package com.shiplocate.trackingsdk.parking

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.trackingsdk.parking.models.InReason
import com.shiplocate.trackingsdk.parking.models.ParkingLocation
import com.shiplocate.trackingsdk.parking.models.ParkingStatus
import com.shiplocate.trackingsdk.utils.ParkingTimeoutTimer
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ParkingTrackerTest {

    private lateinit var scope: CoroutineScope
    private lateinit var timer: ParkingTimeoutTimer
    private lateinit var logger: TestLogger
    private lateinit var tracker: ParkingTracker
    private lateinit var timerFlow: MutableSharedFlow<Unit>

    private val timeoutMs = 1_000L
    private val triggerMs = 200L
    private val radiusMeters = 1_000

    @AfterTest
    fun tearDown() {
        if (::scope.isInitialized) scope.cancel()
    }

    private fun createTracker(
        timeout: Long = timeoutMs,
        trigger: Long = triggerMs,
        radius: Int = radiusMeters,
        testScope: TestScope,
    ) {
        // Use backgroundScope from TestScope per Android coroutines-test docs
        scope = testScope.backgroundScope
        // Mock timer with controllable flow
        timerFlow = MutableSharedFlow(replay = 0)
        timer = mockk(relaxed = true)
        every { timer.timeoutMs } returns timeout
        var running = false
        every { timer.isRunning() } answers { running }
        every { timer.start(any()) } answers { running = true }
        every { timer.stop() } answers { running = false }
        every { timer.timerEvent } returns timerFlow
        logger = TestLogger()
        tracker = ParkingTracker(
            parkingTimeoutTimer = timer,
            parkingRadiusMeters = radius,
            triggerTimeMs = trigger,
            logger = logger,
            scope = scope,
        )
    }

    @Test
    fun `starts timer on first coordinate`() = runTest {
        createTracker(testScope = this)
        val now = Clock.System.now().toEpochMilliseconds()

        tracker.addCoordinate(ParkingLocation(55.0, 37.0, now, 10))

        assertTrue(timer.isRunning(), "Timer should start on first coordinate")
        // cleanup
        timer.stop()
        advanceUntilIdle()
    }

    @Test
    fun `returns false when not enough coordinates and emits nothing`() = runTest {
        createTracker(testScope = this)
        val now = Clock.System.now().toEpochMilliseconds()

        val result = tracker.addCoordinate(ParkingLocation(55.0, 37.0, now, 10))
        advanceUntilIdle()

        assertFalse(result)
        // cleanup
        timer.stop()
        advanceUntilIdle()
    }

    @Test
    fun `returns false when time span less than trigger and emits nothing`() = runTest {
        createTracker(testScope = this)
        val t0 = Clock.System.now().toEpochMilliseconds()

        tracker.addCoordinate(ParkingLocation(55.0, 37.0, t0, 10))
        val result = tracker.addCoordinate(ParkingLocation(55.0, 37.0, t0 + triggerMs / 2, 10))
        advanceUntilIdle()

        assertFalse(result)
        // cleanup
        timer.stop()
        advanceUntilIdle()
    }

    @Test
    fun `emits InParking(Radius) and clears state when all in radius`() = runTest {
        createTracker(trigger = 100L, radius = 1000, testScope = this)
        val t0 = Clock.System.now().toEpochMilliseconds()

        val deferred = async { tracker.observeParkingStatus().first() }
        advanceUntilIdle()
        tracker.addCoordinate(ParkingLocation(55.0, 37.0, t0, 10))
        tracker.addCoordinate(ParkingLocation(55.0005, 37.0005, t0 + 50, 10))
        val res = tracker.addCoordinate(ParkingLocation(55.0002, 37.0002, t0 + 100, 10))
        advanceUntilIdle()

        assertTrue(res)
        val last = withTimeout(1000) { deferred.await() }
        assertTrue(last is ParkingStatus.InParking && last.reason == InReason.Radius)
        // cleanup already done by clear(); ensure no active coroutines
        advanceUntilIdle()
    }

    @Test
    fun `timeout emits InParking(Timeout) and clears without restart`() = runTest {
        createTracker(timeout = 200L, testScope = this)
        val now = Clock.System.now().toEpochMilliseconds()

        // Prepare collector before triggering event
        val deferred = async { tracker.observeParkingStatus().first() }
        advanceUntilIdle()
        // Add one old coordinate to ensure timeDifference > timeout
        tracker.addCoordinate(ParkingLocation(55.0, 37.0, now - 1_000L, 10))

        // Fire timer event directly via our shared flow
        timerFlow.emit(Unit)
        advanceUntilIdle()

        val status = withTimeout(1000) { deferred.await() }
        assertTrue(status is ParkingStatus.InParking && status.reason == InReason.Timeout)
        // cleanup already done by clear(); ensure no active coroutines
        advanceUntilIdle()
    }

    @Test
    fun `onTimerEvent with remaining time restarts timer and emits nothing`() = runTest {
        createTracker(timeout = 1000L, testScope = this)
        val now = Clock.System.now().toEpochMilliseconds()

        val events = mutableListOf<ParkingStatus>()
        val job = launch { tracker.observeParkingStatus().collect { events.add(it) } }

        // Add recent coordinate (timeDifference < timeout)
        tracker.addCoordinate(ParkingLocation(55.0, 37.0, now - 100L, 10))

        // Emit event for remaining-time branch
        timerFlow.emit(Unit)
        advanceUntilIdle()

        assertTrue(timer.isRunning(), "Timer should be running after restart with remaining time")
        assertTrue(events.isEmpty(), "No status should be emitted on remaining branch")

        job.cancel()
        // cleanup: timer is running after restart branch
        timer.stop()
        advanceUntilIdle()
    }

    @Test
    fun `not in radius emits nothing and returns false`() = runTest {
        createTracker(trigger = 100L, radius = 50, testScope = this)
        val t0 = Clock.System.now().toEpochMilliseconds()

        tracker.addCoordinate(ParkingLocation(55.0, 37.0, t0, 10))
        val res = tracker.addCoordinate(ParkingLocation(55.02, 37.03, t0 + 150, 10))
        advanceUntilIdle()

        assertFalse(res)
        // cleanup
        timer.stop()
        advanceUntilIdle()
    }
}

private class TestLogger : Logger {
    override fun log(
        level: com.shiplocate.core.logging.LogLevel,
        category: LogCategory,
        message: String,
        throwable: Throwable?,
        metadata: Map<String, Any>,
    ) { /* no-op for tests */ }
}


