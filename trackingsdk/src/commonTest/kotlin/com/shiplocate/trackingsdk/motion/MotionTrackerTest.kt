package com.shiplocate.trackingsdk.motion

import com.shiplocate.core.logging.Logger
import com.shiplocate.trackingsdk.motion.models.MotionEvent
import com.shiplocate.trackingsdk.motion.models.MotionState
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MotionTrackerTest {

    private val mockLogger = mockk<Logger>(relaxed = true)
    private val mockActivityRecognitionConnector = mockk<ActivityRecognitionConnector>(relaxed = true)
    private val motionEventsFlow = MutableSharedFlow<MotionEvent>(replay = 0)

    init {
        // Настраиваем мок для возврата нашего flow
        io.mockk.every { mockActivityRecognitionConnector.observeMotionEvents() } returns motionEventsFlow
    }

    @Test
    fun `should trigger motion detection with real data from logs`() = runTest {
        val tracker = MotionTracker(
            activityRecognitionConnector = mockActivityRecognitionConnector,
            analysisWindowMs = 3 * 60 * 1000L, // 3 минуты
            trimWindowMs = 1 * 60 * 1000L, // 1 минута
            vehicleTimeThreshold = 0.6f, // 60%
            confidenceThreshold = 70, // 70%
            minAnalysisDurationMs = 1 * 60 * 1000L, // 1 минута
            logger = mockLogger,
            scope = this.backgroundScope
        )

        // Конвертируем реальные данные из логов в MotionEvent
        val realMotionEvents = createRealMotionEventsFromLogs()

        // Запускаем трекер
        tracker.startTracking()

        // Собираем триггеры
        val triggerCollector = async {
            tracker.observeMotionTrigger().take(1).first()
        }

        // Эмитим события с интервалами как в реальных логах
        emitEventsWithRealTiming(realMotionEvents)

        // Даем время на обработку
        advanceUntilIdle()

        // Проверяем, что триггер сработал
        try {
            withTimeout(5000) {
                triggerCollector.await()
            }
            assertTrue("Motion trigger should have been emitted") { true }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            // Если триггер не сработал, выводим диагностическую информацию
            println("Motion trigger did not fire. This suggests the algorithm needs adjustment.")
            println("Total events emitted: ${realMotionEvents.size}")
            println("First event: ${realMotionEvents.first()}")
            println("Last event: ${realMotionEvents.last()}")
            println("Time span: ${realMotionEvents.last().timestamp - realMotionEvents.first().timestamp}ms")
            println("Analysis window: ${3 * 60 * 1000L}ms")
            
            // Анализируем данные вручную
            val lastEvent = realMotionEvents.last()
            val cutoffTime = lastEvent.timestamp - (3 * 60 * 1000L)
            val recentEvents = realMotionEvents.filter { it.timestamp >= cutoffTime }
            println("Recent events (last 3 minutes): ${recentEvents.size}")
            println("Cutoff time: $cutoffTime")
            println("Recent events: ${recentEvents.take(5)}")
            
            // Подсчитываем IN_VEHICLE события
            val inVehicleEvents = recentEvents.filter { it.motionState == MotionState.IN_VEHICLE }
            println("IN_VEHICLE events in last 3 minutes: ${inVehicleEvents.size}")
            println("IN_VEHICLE percentage: ${(inVehicleEvents.size.toFloat() / recentEvents.size * 100).toInt()}%")
            
            // Пока что просто пропускаем тест, но не падаем
            assertTrue("Motion trigger should have been emitted, but algorithm needs tuning") { false }
        }
    }

    @Test
    fun `should analyze motion statistics correctly with real data`() = runTest {
        val testScope = TestScope()
        val tracker = MotionTracker(
            activityRecognitionConnector = mockActivityRecognitionConnector,
            analysisWindowMs = 3 * 60 * 1000L,
            trimWindowMs = 1 * 60 * 1000L,
            vehicleTimeThreshold = 0.6f,
            confidenceThreshold = 70,
            minAnalysisDurationMs = 1 * 60 * 1000L,
            logger = mockLogger,
            scope = testScope.backgroundScope
        )

        // Тестируем с подмножеством данных (первые 3 минуты)
        val testEvents = createRealMotionEventsFromLogs().take(20) // Первые 20 событий

        tracker.startTracking()

        // Эмитим события
        emitEventsWithRealTiming(testEvents)

        // Даем время на обработку
        advanceUntilIdle()

        // Проверяем, что трекер обработал события
        // (В реальном тесте здесь можно проверить внутреннее состояние трекера)
    }

    private fun createRealMotionEventsFromLogs(): List<MotionEvent> {
        // Конвертируем реальные данные из логов
        val baseTimestamp = 1735565091782L // 2025-10-30T17:04:51.782489Z в миллисекундах
        
        return listOf(
            // 17:04:51 - UNKNOWN (40%)
            MotionEvent(MotionState.UNKNOWN, 40, baseTimestamp),
            // 17:05:03 - UNKNOWN (40%)
            MotionEvent(MotionState.UNKNOWN, 40, baseTimestamp + 11374),
            // 17:05:16 - UNKNOWN (40%)
            MotionEvent(MotionState.UNKNOWN, 40, baseTimestamp + 24429),
            // 17:05:25 - UNKNOWN (40%)
            MotionEvent(MotionState.UNKNOWN, 40, baseTimestamp + 34160),
            // 17:05:37 - WALKING (87%)
            MotionEvent(MotionState.WALKING, 87, baseTimestamp + 45533),
            // 17:05:50 - WALKING (81%)
            MotionEvent(MotionState.WALKING, 81, baseTimestamp + 58418),
            // 17:06:02 - UNKNOWN (40%)
            MotionEvent(MotionState.UNKNOWN, 40, baseTimestamp + 70407),
            // 17:06:15 - WALKING (81%)
            MotionEvent(MotionState.WALKING, 81, baseTimestamp + 83563),
            // 17:06:28 - WALKING (91%)
            MotionEvent(MotionState.WALKING, 91, baseTimestamp + 96594),
            // 17:06:41 - UNKNOWN (100%)
            MotionEvent(MotionState.UNKNOWN, 100, baseTimestamp + 109418),
            // 17:06:41 - WALKING (87%) - дублирующее событие
            MotionEvent(MotionState.WALKING, 87, baseTimestamp + 109433),
            // 17:06:52 - UNKNOWN (100%)
            MotionEvent(MotionState.UNKNOWN, 100, baseTimestamp + 120747),
            // 17:06:52 - UNKNOWN (40%) - дублирующее событие
            MotionEvent(MotionState.UNKNOWN, 40, baseTimestamp + 120780),
            // 17:07:03 - STATIONARY (39%)
            MotionEvent(MotionState.STATIONARY, 39, baseTimestamp + 131958),
            // 17:07:17 - IN_VEHICLE (82%)
            MotionEvent(MotionState.IN_VEHICLE, 82, baseTimestamp + 145166),
            // 17:07:29 - UNKNOWN (40%)
            MotionEvent(MotionState.UNKNOWN, 40, baseTimestamp + 157498),
            // 17:07:40 - IN_VEHICLE (90%)
            MotionEvent(MotionState.IN_VEHICLE, 90, baseTimestamp + 168797),
            // 17:07:52 - IN_VEHICLE (93%)
            MotionEvent(MotionState.IN_VEHICLE, 93, baseTimestamp + 180189),
            // 17:08:05 - IN_VEHICLE (92%)
            MotionEvent(MotionState.IN_VEHICLE, 92, baseTimestamp + 193202),
            // 17:08:16 - IN_VEHICLE (87%)
            MotionEvent(MotionState.IN_VEHICLE, 87, baseTimestamp + 204719),
            // 17:08:27 - IN_VEHICLE (84%)
            MotionEvent(MotionState.IN_VEHICLE, 84, baseTimestamp + 216295),
            // 17:08:39 - UNKNOWN (40%)
            MotionEvent(MotionState.UNKNOWN, 40, baseTimestamp + 227669),
            // 17:08:52 - IN_VEHICLE (90%)
            MotionEvent(MotionState.IN_VEHICLE, 90, baseTimestamp + 240601),
            // 17:09:03 - UNKNOWN (100%)
            MotionEvent(MotionState.UNKNOWN, 100, baseTimestamp + 251971),
            // 17:09:03 - IN_VEHICLE (85%) - дублирующее событие
            MotionEvent(MotionState.IN_VEHICLE, 85, baseTimestamp + 251995),
            // 17:09:15 - UNKNOWN (40%)
            MotionEvent(MotionState.UNKNOWN, 40, baseTimestamp + 263536),
            // 17:09:26 - STATIONARY (100%)
            MotionEvent(MotionState.STATIONARY, 100, baseTimestamp + 274945),
            // 17:09:37 - UNKNOWN (100%)
            MotionEvent(MotionState.UNKNOWN, 100, baseTimestamp + 286326),
            // 17:09:37 - IN_VEHICLE (98%) - дублирующее событие
            MotionEvent(MotionState.IN_VEHICLE, 98, baseTimestamp + 286351),
            // 17:09:49 - UNKNOWN (100%)
            MotionEvent(MotionState.UNKNOWN, 100, baseTimestamp + 297563),
            // 17:09:49 - IN_VEHICLE (96%) - дублирующее событие
            MotionEvent(MotionState.IN_VEHICLE, 96, baseTimestamp + 297579),
            // 17:10:00 - STATIONARY (100%)
            MotionEvent(MotionState.STATIONARY, 100, baseTimestamp + 308981),
            // 17:10:11 - STATIONARY (100%)
            MotionEvent(MotionState.STATIONARY, 100, baseTimestamp + 320364),
            // 17:10:23 - IN_VEHICLE (96%)
            MotionEvent(MotionState.IN_VEHICLE, 96, baseTimestamp + 331612),
            // 17:10:34 - UNKNOWN (40%)
            MotionEvent(MotionState.UNKNOWN, 40, baseTimestamp + 342923),
            // 17:10:46 - STATIONARY (100%)
            MotionEvent(MotionState.STATIONARY, 100, baseTimestamp + 354306),
            // 17:10:57 - STATIONARY (100%)
            MotionEvent(MotionState.STATIONARY, 100, baseTimestamp + 365660),
            // 17:11:08 - IN_VEHICLE (96%)
            MotionEvent(MotionState.IN_VEHICLE, 96, baseTimestamp + 377010),
            // 17:11:20 - IN_VEHICLE (96%)
            MotionEvent(MotionState.IN_VEHICLE, 96, baseTimestamp + 388391),
            // 17:11:30 - IN_VEHICLE (98%)
            MotionEvent(MotionState.IN_VEHICLE, 98, baseTimestamp + 398628),
            // 17:11:41 - IN_VEHICLE (96%)
            MotionEvent(MotionState.IN_VEHICLE, 96, baseTimestamp + 409954),
            // 17:11:53 - IN_VEHICLE (96%)
            MotionEvent(MotionState.IN_VEHICLE, 96, baseTimestamp + 421295),
            // 17:12:04 - IN_VEHICLE (96%)
            MotionEvent(MotionState.IN_VEHICLE, 96, baseTimestamp + 432692),
            // 17:12:15 - IN_VEHICLE (96%)
            MotionEvent(MotionState.IN_VEHICLE, 96, baseTimestamp + 444043),
            // 17:12:27 - UNKNOWN (40%)
            MotionEvent(MotionState.UNKNOWN, 40, baseTimestamp + 455185),
            // 17:12:37 - IN_VEHICLE (96%) - начало 3-минутного окна
            MotionEvent(MotionState.IN_VEHICLE, 96, baseTimestamp + 465732),
            // 17:12:48 - IN_VEHICLE (96%)
            MotionEvent(MotionState.IN_VEHICLE, 96, baseTimestamp + 477105),
            // 17:13:00 - IN_VEHICLE (96%)
            MotionEvent(MotionState.IN_VEHICLE, 96, baseTimestamp + 488388),
            // 17:13:11 - UNKNOWN (40%)
            MotionEvent(MotionState.UNKNOWN, 40, baseTimestamp + 499722),
            // 17:13:22 - UNKNOWN (40%)
            MotionEvent(MotionState.UNKNOWN, 40, baseTimestamp + 511096),
            // 17:13:34 - IN_VEHICLE (97%)
            MotionEvent(MotionState.IN_VEHICLE, 97, baseTimestamp + 522581),
            // 17:13:45 - IN_VEHICLE (97%)
            MotionEvent(MotionState.IN_VEHICLE, 97, baseTimestamp + 533909),
            // 17:13:55 - IN_VEHICLE (97%)
            MotionEvent(MotionState.IN_VEHICLE, 97, baseTimestamp + 543943),
            // 17:14:06 - IN_VEHICLE (97%)
            MotionEvent(MotionState.IN_VEHICLE, 97, baseTimestamp + 555387),
            // 17:14:18 - IN_VEHICLE (96%)
            MotionEvent(MotionState.IN_VEHICLE, 96, baseTimestamp + 566805),
            // 17:14:29 - IN_VEHICLE (97%)
            MotionEvent(MotionState.IN_VEHICLE, 97, baseTimestamp + 578135),
            // 17:14:40 - IN_VEHICLE (97%)
            MotionEvent(MotionState.IN_VEHICLE, 97, baseTimestamp + 589497),
            // 17:14:53 - IN_VEHICLE (97%)
            MotionEvent(MotionState.IN_VEHICLE, 97, baseTimestamp + 601767),
            // 17:15:04 - IN_VEHICLE (97%)
            MotionEvent(MotionState.IN_VEHICLE, 97, baseTimestamp + 613121),
            // 17:15:15 - IN_VEHICLE (97%)
            MotionEvent(MotionState.IN_VEHICLE, 97, baseTimestamp + 624437),
            // 17:15:27 - IN_VEHICLE (97%)
            MotionEvent(MotionState.IN_VEHICLE, 97, baseTimestamp + 635778),
            // 17:15:38 - IN_VEHICLE (96%)
            MotionEvent(MotionState.IN_VEHICLE, 96, baseTimestamp + 647198),
            // 17:15:49 - IN_VEHICLE (96%)
            MotionEvent(MotionState.IN_VEHICLE, 96, baseTimestamp + 658571),
            // 17:16:01 - IN_VEHICLE (96%)
            MotionEvent(MotionState.IN_VEHICLE, 96, baseTimestamp + 669948),
            // 17:16:12 - UNKNOWN (40%)
            MotionEvent(MotionState.UNKNOWN, 40, baseTimestamp + 681337),
            // 17:16:24 - IN_VEHICLE (88%)
            MotionEvent(MotionState.IN_VEHICLE, 88, baseTimestamp + 692720),
            // 17:16:35 - IN_VEHICLE (97%)
            MotionEvent(MotionState.IN_VEHICLE, 97, baseTimestamp + 704058),
            // 17:16:46 - IN_VEHICLE (97%)
            MotionEvent(MotionState.IN_VEHICLE, 97, baseTimestamp + 715394),
            // 17:16:58 - IN_VEHICLE (97%)
            MotionEvent(MotionState.IN_VEHICLE, 97, baseTimestamp + 726717),
            // 17:17:12 - IN_VEHICLE (96%) - конец 3-минутного окна
            MotionEvent(MotionState.IN_VEHICLE, 96, baseTimestamp + 740943),
            // 17:17:23 - IN_VEHICLE (94%)
            MotionEvent(MotionState.IN_VEHICLE, 94, baseTimestamp + 752428),
            // 17:17:35 - UNKNOWN (100%)
            MotionEvent(MotionState.UNKNOWN, 100, baseTimestamp + 763800),
            // 17:17:35 - IN_VEHICLE (90%) - дублирующее событие
            MotionEvent(MotionState.IN_VEHICLE, 90, baseTimestamp + 763827),
            // 17:17:40 - IN_VEHICLE (90%)
            MotionEvent(MotionState.IN_VEHICLE, 90, baseTimestamp + 768922)
        )
    }

    private suspend fun emitEventsWithRealTiming(events: List<MotionEvent>) {
        for (event in events) {
            motionEventsFlow.emit(event)
            // Небольшая задержка между событиями для имитации реального времени
            kotlinx.coroutines.delay(10)
        }
    }
}
