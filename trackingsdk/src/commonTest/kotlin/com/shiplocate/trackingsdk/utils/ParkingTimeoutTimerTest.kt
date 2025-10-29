package com.shiplocate.trackingsdk.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ParkingTimeoutTimerTest {

    private lateinit var timerScope: CoroutineScope
    private lateinit var timer: ParkingTimeoutTimer

    @AfterTest
    fun tearDown() {
        if (::timerScope.isInitialized) {
            timerScope.cancel()
        }
    }

    @Test
    fun `start should set isRunning to true`() = runTest {
        // Arrange - используем TestScope для создания CoroutineScope
        timerScope = CoroutineScope(coroutineContext)
        timer = ParkingTimeoutTimer(timeoutMs = 1000L, scope = timerScope)

        // Act
        timer.start(delayMs = 100L)

        // Assert
        assertTrue(timer.isRunning(), "Timer should be running after start")

        // Cleanup
        timer.stop()
        advanceUntilIdle() // Ensure all coroutines complete
    }

    @Test
    fun `timer should emit event after delay`() = runTest {
        // Arrange
        timerScope = CoroutineScope(coroutineContext)
        timer = ParkingTimeoutTimer(timeoutMs = 1000L, scope = timerScope)
        val delayMs = 100L

        // Start collecting events before starting timer
        val events = mutableListOf<Unit>()
        val collectJob = launch {
            timer.timerEvent.collect { events.add(it) }
        }

        // Act
        timer.start(delayMs = delayMs)

        // Advance time to trigger the delay
        advanceTimeBy(delayMs)
        advanceUntilIdle() // Ensure all coroutines complete

        // Cancel collection job
        collectJob.cancel()

        // Assert
        assertTrue(events.isNotEmpty() && events.size == 1, "Timer should emit event after delay")
        assertFalse(timer.isRunning(), "Timer should not be running after event is emitted")
    }

    @Test
    fun `timer should not emit event before delay completes`() = runTest {
        // Arrange
        timerScope = CoroutineScope(coroutineContext)
        timer = ParkingTimeoutTimer(timeoutMs = 1000L, scope = timerScope)
        val delayMs = 100L

        // Start collecting events before starting timer
        val events = mutableListOf<Unit>()
        val collectJob = launch {
            timer.timerEvent.collect { events.add(it) }
        }

        // Act
        timer.start(delayMs = delayMs)

        // Advance time, but not enough to trigger the delay (use delayMs - 1 to be safe)
        advanceTimeBy(delayMs - 1)
        // Don't call advanceUntilIdle() here as it might advance time further

        // Assert - check state before delay completes
        assertTrue(events.isEmpty(), "Timer should not emit event before delay completes. Events: ${events.size}")
        assertTrue(timer.isRunning(), "Timer should still be running before delay completes")

        // Cancel collection job
        collectJob.cancel()
    }

    @Test
    fun `stop should cancel timer and set isRunning to false`() = runTest {
        // Arrange
        timerScope = CoroutineScope(coroutineContext)
        timer = ParkingTimeoutTimer(timeoutMs = 1000L, scope = timerScope)

        // Act
        timer.start(delayMs = 100L)
        assertTrue(timer.isRunning(), "Timer should be running")

        timer.stop()
        advanceUntilIdle() // Ensure cancellation completes

        // Assert
        assertFalse(timer.isRunning(), "Timer should not be running after stop")
    }

    @Test
    fun `stop should prevent event emission`() = runTest {
        // Arrange
        timerScope = CoroutineScope(coroutineContext)
        timer = ParkingTimeoutTimer(timeoutMs = 1000L, scope = timerScope)
        val delayMs = 100L

        // Start collecting events
        val events = mutableListOf<Unit>()
        val collectJob = launch {
            timer.timerEvent.collect { events.add(it) }
        }

        // Act
        timer.start(delayMs = delayMs)
        timer.stop()

        // Advance time beyond delay
        advanceTimeBy(delayMs + 10L)
        advanceUntilIdle() // Ensure all coroutines complete

        // Cancel collection job
        collectJob.cancel()

        // Assert
        assertTrue(events.isEmpty(), "Timer should not emit event after stop")
        assertFalse(timer.isRunning(), "Timer should not be running after stop")
    }

    @Test
    fun `start should not restart if already running`() = runTest {
        // Arrange
        timerScope = CoroutineScope(coroutineContext)
        timer = ParkingTimeoutTimer(timeoutMs = 1000L, scope = timerScope)

        // Act
        timer.start(delayMs = 100L)
        val initialRunningState = timer.isRunning()

        // Try to start again
        timer.start(delayMs = 200L)

        // Assert
        assertTrue(timer.isRunning(), "Timer should still be running")
        assertTrue(initialRunningState, "Timer should have been running before second start")

        // Cleanup
        timer.stop()
        advanceUntilIdle() // Ensure all coroutines complete
    }

    @Test
    fun `stop should do nothing if timer is not running`() = runTest {
        // Arrange
        timerScope = CoroutineScope(coroutineContext)
        timer = ParkingTimeoutTimer(timeoutMs = 1000L, scope = timerScope)

        // Act & Assert - should not throw
        assertFalse(timer.isRunning(), "Timer should not be running initially")
        timer.stop()
        assertFalse(timer.isRunning(), "Timer should still not be running after stop")
    }

    @Test
    fun `timer should emit event only once after delay`() = runTest {
        // Arrange
        timerScope = CoroutineScope(coroutineContext)
        timer = ParkingTimeoutTimer(timeoutMs = 1000L, scope = timerScope)
        val delayMs = 100L

        // Start collecting events
        val events = mutableListOf<Unit>()
        val collectJob = launch {
            timer.timerEvent.collect { events.add(it) }
        }

        // Act
        timer.start(delayMs = delayMs)
        advanceTimeBy(delayMs)
        advanceUntilIdle() // Ensure all coroutines complete

        // Wait a bit more to ensure no more events
        advanceTimeBy(50L)
        advanceUntilIdle()

        // Cancel collection job
        collectJob.cancel()

        // Assert
        assertEquals(1, events.size, "Timer should emit exactly one event")
        assertFalse(timer.isRunning(), "Timer should not be running after event")
    }

    @Test
    fun `timer should handle zero delay`() = runTest {
        // Arrange
        timerScope = CoroutineScope(coroutineContext)
        timer = ParkingTimeoutTimer(timeoutMs = 1000L, scope = timerScope)

        // Start collecting events
        val events = mutableListOf<Unit>()
        val collectJob = launch {
            timer.timerEvent.collect { events.add(it) }
        }

        // Act
        timer.start(delayMs = 0L)
        advanceTimeBy(1L)
        advanceUntilIdle() // Ensure all coroutines complete

        // Cancel collection job
        collectJob.cancel()

        // Assert
        assertTrue(events.isNotEmpty(), "Timer should emit event even with zero delay")
        assertFalse(timer.isRunning(), "Timer should not be running after event")
    }

    @Test
    fun `timer should handle multiple start stop cycles`() = runTest {
        // Arrange
        timerScope = CoroutineScope(coroutineContext)
        timer = ParkingTimeoutTimer(timeoutMs = 1000L, scope = timerScope)

        // Act & Assert - First cycle
        timer.start(delayMs = 100L)
        assertTrue(timer.isRunning(), "Timer should be running after first start")

        timer.stop()
        advanceUntilIdle()
        assertFalse(timer.isRunning(), "Timer should not be running after first stop")

        // Second cycle
        timer.start(delayMs = 100L)
        assertTrue(timer.isRunning(), "Timer should be running after second start")

        timer.stop()
        advanceUntilIdle()
        assertFalse(timer.isRunning(), "Timer should not be running after second stop")
    }

    @Test
    fun `timer should not emit event if stopped before delay`() = runTest {
        // Arrange
        timerScope = CoroutineScope(coroutineContext)
        timer = ParkingTimeoutTimer(timeoutMs = 1000L, scope = timerScope)
        val delayMs = 100L

        // Start collecting events
        val events = mutableListOf<Unit>()
        val collectJob = launch {
            timer.timerEvent.collect { events.add(it) }
        }

        // Act
        timer.start(delayMs = delayMs)
        advanceTimeBy(delayMs / 2) // Advance half way
        timer.stop()
        advanceTimeBy(delayMs) // Advance past the delay
        advanceUntilIdle() // Ensure all coroutines complete

        // Cancel collection job
        collectJob.cancel()

        // Assert
        assertTrue(events.isEmpty(), "Timer should not emit event if stopped before delay completes")
        assertFalse(timer.isRunning(), "Timer should not be running after stop")
    }
}

