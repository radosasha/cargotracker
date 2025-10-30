package com.shiplocate.trackingsdk.motion

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.trackingsdk.motion.models.MotionAnalysisEvent
import com.shiplocate.trackingsdk.motion.models.MotionState
import com.shiplocate.trackingsdk.motion.models.MotionStatistics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Трекер движения пользователя
 * Анализирует активность пользователя и определяет, находится ли он в транспорте
 *
 * Алгоритм определения движения в транспорте:
 * 1. Собирает статистику активности за период, равный analysisWindowMs
 * 2. Если доля времени в IN_VEHICLE >= vehicleTimeThreshold и confidence >= confidenceThreshold — считаем, что в транспорте
 * 3. Дополнительные проверки (при необходимости): скорость, ускорение, паттерны движения
 * 4. Фильтрация ложных срабатываний (кратковременные переходы)
 */
class MotionTracker(
    private val activityRecognitionConnector: ActivityRecognitionConnector,
    val analysisWindowMs: Long = 3 * 60 * 1000L,
    // 1 минута для обрезки истории
    val trimWindowMs: Long = 1 * 60 * 1000L,
    // Пороги для определения движения в транспорте (обновлены для работы с реальными вероятностями)
    // 60% времени в транспорте (снижено, так как теперь у нас реальные вероятности)
    val vehicleTimeThreshold: Float = 0.6f,
    // 70% уверенности (снижено, так как ActivityRecognition дает более точные данные)
    val confidenceThreshold: Int = 70,
    // Минимум 1 минута анализа (снижено для более быстрого реагирования)
    val minAnalysisDurationMs: Long = 1 * 60 * 1000,
    val logger: Logger,
    private val scope: CoroutineScope,
) {


    // Буфер событий движения за текущее аналитическое окно (analysisWindowMs)
    private val motionHistory = mutableListOf<MotionAnalysisEvent>()

    // Flow для уведомления о движении в транспорте
    private val observeMotionTrigger = MutableSharedFlow<Unit>(replay = 0)

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
        activityRecognitionConnector.observeMotionEvents().onEach { motionEvent ->
            logger.debug(
                LogCategory.LOCATION,
                "$TAG: Received motion event: ${motionEvent.motionState} (confidence: ${motionEvent.confidence}%)"
            )

            // Добавляем событие в историю
            val analysisResult = MotionAnalysisEvent(
                motionState = motionEvent.motionState,
                confidence = motionEvent.confidence,
                timestamp = motionEvent.timestamp
            )
            motionHistory.add(analysisResult)

            // Проверяем, нужно ли начать анализ. Анализ запускается,
            // когда накопленный временной интервал >= analysisWindowMs
            if (shouldStartAnalysis()) {
                performAnalysis()
            }
        }.launchIn(scope)
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

    fun observeMotionTrigger(): Flow<Unit> = observeMotionTrigger

    /**
     * Проверяет, нужно ли начать анализ на основе накопленных данных
     */
    private fun shouldStartAnalysis(): Boolean {
        if (motionHistory.size < 2) {
            logger.debug(LogCategory.LOCATION, "$TAG: shouldStartAnalysis: size=${motionHistory.size} < 2")
            return false
        }

        val lastEvent = motionHistory.last()
        val cutoffTime = lastEvent.timestamp - analysisWindowMs
        
        // Проверяем, есть ли достаточно данных за последние analysisWindowMs
        val recentEvents = motionHistory.filter { it.timestamp >= cutoffTime }
        val result = recentEvents.size >= 2
        
        logger.debug(LogCategory.LOCATION, "$TAG: shouldStartAnalysis: size=${motionHistory.size}, recentEvents=${recentEvents.size}, cutoffTime=$cutoffTime, lastEvent=${lastEvent.timestamp}, analysisWindowMs=$analysisWindowMs, result=$result")
        
        return result
    }

    /**
     * Выполняет анализ накопленных данных
     */
    private suspend fun performAnalysis() {
        logger.debug(LogCategory.LOCATION, "$TAG: Starting analysis of ${motionHistory.size} events")

        val statistics = calculateMotionStatistics()

        if (isInVehicle(statistics)) {
            logger.info(LogCategory.LOCATION, "$TAG: Vehicle motion detected based on analysis")
            observeMotionTrigger.emit(Unit)
            // Сразу очищаем историю, чтобы избежать повторных мгновенных триггеров
            // при поступлении следующего события без достаточного нового окна данных
            clear()
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

        val lastEvent = motionHistory.last()
        val cutoffTime = lastEvent.timestamp - analysisWindowMs
        
        // Анализируем только последние analysisWindowMs данных
        val recentEvents = motionHistory.filter { it.timestamp >= cutoffTime }
        
        if (recentEvents.size < 2) {
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

        val firstEvent = recentEvents.first()
        val totalTimeMs = lastEvent.timestamp - firstEvent.timestamp

        // Вычисляем время для всех состояний сразу
        // Один проход по данным, группируем по состояниям
        val timeByState = recentEvents.zipWithNext { current, next ->
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

        val lastActivity = recentEvents.lastOrNull()?.motionState ?: MotionState.UNKNOWN
        // Усредняем confidence с учетом длительности интервалов между событиями
        val intervals = recentEvents.zipWithNext { a, b ->
            val dt = (b.timestamp - a.timestamp).coerceAtLeast(0L)
            val conf = a.confidence
            dt to conf
        }
        val totalDt = intervals.sumOf { it.first }
        val weightedConfidence = if (totalDt > 0L) {
            (intervals.sumOf { it.first * it.second } / totalDt).toInt()
        } else 0

        return MotionStatistics(
            totalTimeMs = totalTimeMs,
            vehicleTimeMs = vehicleTimeMs,
            walkingTimeMs = walkingTimeMs,
            stationaryTimeMs = stationaryTimeMs,
            vehiclePercentage = vehiclePercentage,
            lastActivity = lastActivity,
            confidence = weightedConfidence
        )
    }

    /**
     * Определяет, находится ли пользователь в транспорте на основе статистики
     */
    private fun isInVehicle(statistics: MotionStatistics): Boolean {
        // Логика определения «в транспорте» основывается на:
        // 1) доле времени в IN_VEHICLE (vehiclePercentage) за окно analysisWindowMs,
        // 2) усредненном confidence, рассчитанном с учетом времени между событиями,
        // 3) минимальной длительности накопленных данных (minAnalysisDurationMs).
        val isVehicleTimeSufficient = statistics.vehiclePercentage >= vehicleTimeThreshold
        val isConfidenceSufficient = statistics.confidence >= confidenceThreshold
        val isDurationSufficient = statistics.totalTimeMs >= minAnalysisDurationMs

        val result = isVehicleTimeSufficient && isConfidenceSufficient && isDurationSufficient

        logger.debug(LogCategory.LOCATION, "$TAG: Vehicle detection analysis:")
        logger.debug(
            LogCategory.LOCATION,
            "  - Vehicle time: ${(statistics.vehiclePercentage * 100).toInt()}% (threshold: ${(vehicleTimeThreshold * 100).toInt()}%)"
        )
        logger.debug(LogCategory.LOCATION, "  - Confidence: ${statistics.confidence}% (threshold: $confidenceThreshold%)")
        logger.debug(LogCategory.LOCATION, "  - Duration: ${statistics.totalTimeMs}ms (threshold: ${minAnalysisDurationMs}ms)")
        logger.debug(LogCategory.LOCATION, "  - Result: $result")

        return result
    }
}
