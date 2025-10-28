package com.shiplocate.trackingsdk.motion

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.trackingsdk.motion.models.MotionAnalysisEvent
import com.shiplocate.trackingsdk.motion.models.MotionState
import com.shiplocate.trackingsdk.motion.models.MotionStatistics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Трекер движения пользователя
 * Анализирует активность пользователя и определяет, находится ли он в транспорте
 *
 * Алгоритм определения движения в транспорте:
 * 1. Собирает статистику активности за последние 5 минут
 * 2. Если >70% времени в состоянии IN_VEHICLE с уверенностью >80% - считаем что в транспорте
 * 3. Дополнительные проверки: скорость, ускорение, паттерны движения
 * 4. Фильтрация ложных срабатываний (кратковременные переходы)
 */
class MotionTracker(
    private val activityRecognitionConnector: ActivityRecognitionConnector,
    private val logger: Logger
) {

    private val scope = CoroutineScope(Dispatchers.Default)

    // Статистика движения за последние 5 минут
    private val motionHistory = mutableListOf<MotionAnalysisEvent>()
    private val analysisWindowMs = 5 * 60 * 1000L // 5 минут
    private val trimWindowMs = 1 * 60 * 1000L // 1 минута для обрезки истории

    // Пороги для определения движения в транспорте (обновлены для работы с реальными вероятностями)
    private val vehicleTimeThreshold = 0.6f // 60% времени в транспорте (снижено, так как теперь у нас реальные вероятности)
    private val confidenceThreshold = 70 // 70% уверенности (снижено, так как ActivityRecognition дает более точные данные)
    private val minAnalysisDurationMs = 1 * 60 * 1000L // Минимум 1 минута анализа (снижено для более быстрого реагирования)
    private val minVehicleConfidenceThreshold = 75 // Минимальная уверенность для IN_VEHICLE события

    // Flow для статистики движения
    private val _motionStatistics = MutableSharedFlow<MotionStatistics>(replay = 0)
    val motionStatistics: SharedFlow<MotionStatistics> = _motionStatistics.asSharedFlow()

    // Flow для уведомления о движении в транспорте
    private val _vehicleMotionEvent = MutableSharedFlow<Unit>(replay = 0)
    val vehicleMotionEvent: SharedFlow<Unit> = _vehicleMotionEvent.asSharedFlow()

    companion object {
        private val TAG = MotionTracker::class.simpleName
    }

    /**
     * Запускает отслеживание движения через ActivityRecognitionConnector
     */
    fun startTracking() {
        logger.info(LogCategory.LOCATION, "$TAG: Starting motion tracking")

        // Запускаем ActivityRecognitionConnector
        activityRecognitionConnector.startTracking()

        // Подписываемся на события движения от ActivityRecognitionConnector
        scope.launch {
            activityRecognitionConnector.motionEvents.collect { motionEvent ->
                logger.debug(LogCategory.LOCATION, "$TAG: Received motion event: ${motionEvent.motionState} (confidence: ${motionEvent.confidence}%)")

                // Добавляем событие в историю
                val analysisResult = MotionAnalysisEvent(
                    motionState = motionEvent.motionState,
                    confidence = motionEvent.confidence,
                    timestamp = motionEvent.timestamp
                )
                motionHistory.add(analysisResult)

                // Проверяем, нужно ли начать анализ
                if (shouldStartAnalysis()) {
                    performAnalysis()
                }
            }
        }
    }

    /**
     * Останавливает отслеживание движения через ActivityRecognitionConnector
     */
    fun stopTracking() {
        activityRecognitionConnector.stopTracking()
        logger.info(LogCategory.LOCATION, "$TAG: Stopped motion tracking via ActivityRecognitionConnector")
    }

    /**
     * Освобождает ресурсы ActivityRecognitionConnector
     */
    fun destroy() {
        activityRecognitionConnector.destroy()
        clear()
        logger.debug(LogCategory.LOCATION, "$TAG: Destroyed")
    }

    /**
     * Очищает данные трекера
     */
    fun clear() {
        motionHistory.clear()
        logger.debug(LogCategory.LOCATION, "$TAG: Cleared motion history")
    }

    /**
     * Проверяет, нужно ли начать анализ на основе накопленных данных
     */
    private fun shouldStartAnalysis(): Boolean {
        if (motionHistory.size < 2) return false
        
        val firstEvent = motionHistory.first()
        val lastEvent = motionHistory.last()
        val timeSpan = lastEvent.timestamp - firstEvent.timestamp
        
        return timeSpan >= analysisWindowMs
    }

    /**
     * Выполняет анализ накопленных данных
     */
    private suspend fun performAnalysis() {
        logger.debug(LogCategory.LOCATION, "$TAG: Starting analysis of ${motionHistory.size} events")
        
        val statistics = calculateMotionStatistics()
        _motionStatistics.emit(statistics)
        
        if (isInVehicle(statistics)) {
            logger.info(LogCategory.LOCATION, "$TAG: Vehicle motion detected based on analysis")
            _vehicleMotionEvent.emit(Unit)
        } else {
            logger.debug(LogCategory.LOCATION, "$TAG: Not in vehicle, trimming history")
            // Если вероятность движения в транспорте низкая, обрезаем историю
            trimHistory()
        }
    }

    /**
     * Обрезает историю, удаляя старые записи
     */
    private fun trimHistory() {
        if (motionHistory.isEmpty()) return
        
        val currentTime = motionHistory.last().timestamp
        val cutoffTime = currentTime - trimWindowMs
        
        val initialSize = motionHistory.size
        motionHistory.removeAll { result ->
            result.timestamp < cutoffTime
        }
        
        logger.debug(LogCategory.LOCATION, "$TAG: Trimmed history from $initialSize to ${motionHistory.size} events")
    }

    /**
     * Вычисляет статистику движения за анализируемый период с учетом реальных вероятностей
     */
    private fun calculateMotionStatistics(): MotionStatistics {
        if (motionHistory.isEmpty()) {
            return MotionStatistics(
                totalTimeMs = 0L,
                vehicleTimeMs = 0L,
                walkingTimeMs = 0L,
                stationaryTimeMs = 0L,
                vehiclePercentage = 0f,
                lastActivity = MotionState.UNKNOWN,
                confidence = 0
            )
        }
        
        val firstEvent = motionHistory.first()
        val lastEvent = motionHistory.last()
        val totalTimeMs = lastEvent.timestamp - firstEvent.timestamp

        // Вычисляем время для всех состояний сразу
        // Один проход по данным, группируем по состояниям
        val timeByState = motionHistory.zipWithNext { current, next ->
            val intervalMs = next.timestamp - current.timestamp
            val weightedTime = (intervalMs * (current.confidence / 100f)).toLong()
            current.motionState to weightedTime
        }.groupBy({ it.first }, { it.second })
            .mapValues { it.value.sum() }

        val vehicleTimeMs = timeByState[MotionState.IN_VEHICLE] ?: 0L
        val walkingTimeMs = timeByState[MotionState.WALKING] ?: 0L
        val stationaryTimeMs = timeByState[MotionState.STATIONARY] ?: 0L

        val vehiclePercentage = if (totalTimeMs > 0) {
            vehicleTimeMs.toFloat() / totalTimeMs
        } else {
            0f
        }

        val lastActivity = motionHistory.lastOrNull()?.motionState ?: MotionState.UNKNOWN
        val averageConfidence = motionHistory.map { it.confidence }.average().toInt()

        return MotionStatistics(
            totalTimeMs = totalTimeMs,
            vehicleTimeMs = vehicleTimeMs,
            walkingTimeMs = walkingTimeMs,
            stationaryTimeMs = stationaryTimeMs,
            vehiclePercentage = vehiclePercentage,
            lastActivity = lastActivity,
            confidence = averageConfidence
        )
    }

    /**
     * Определяет, находится ли пользователь в транспорте на основе статистики
     */
    private fun isInVehicle(statistics: MotionStatistics): Boolean {
        val isVehicleTimeSufficient = statistics.vehiclePercentage >= vehicleTimeThreshold
        val isConfidenceSufficient = statistics.confidence >= confidenceThreshold
        val isDurationSufficient = statistics.totalTimeMs >= minAnalysisDurationMs

        val result = isVehicleTimeSufficient && isConfidenceSufficient && isDurationSufficient

        logger.debug(LogCategory.LOCATION, "$TAG: Vehicle detection analysis:")
        logger.debug(LogCategory.LOCATION, "  - Vehicle time: ${(statistics.vehiclePercentage * 100).toInt()}% (threshold: ${(vehicleTimeThreshold * 100).toInt()}%)")
        logger.debug(LogCategory.LOCATION, "  - Confidence: ${statistics.confidence}% (threshold: $confidenceThreshold%)")
        logger.debug(LogCategory.LOCATION, "  - Duration: ${statistics.totalTimeMs}ms (threshold: ${minAnalysisDurationMs}ms)")
        logger.debug(LogCategory.LOCATION, "  - Result: $result")

        return result
    }
}
