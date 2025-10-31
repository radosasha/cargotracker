package com.shiplocate.trackingsdk.motion

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.trackingsdk.motion.models.MotionAnalysisEvent
import com.shiplocate.trackingsdk.motion.models.MotionState
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
    val analysisWindowMs: Long = 60 * 1000L,
    // 1 минута для обрезки истории (используется как минимальный размер окна при тримминге)
    val trimWindowMs: Long = 1 * 60 * 1000L,
    // Пороги для определения движения в транспорте (обновлены для работы с реальными вероятностями)
    // 60% времени в транспорте (снижено, так как теперь у нас реальные вероятности)
    val vehicleTimeThreshold: Float = 0.6f,
    // 70% уверенности (снижено, так как ActivityRecognition дает более точные данные)
    val confidenceThreshold: Int = 70,
    // Минимум 1 минута анализа (снижено для более быстрого реагирования)
    val minAnalysisDurationMs: Long = 1 * 60 * 1000,
    // Окно хранения истории (жесткий предел)
    val retentionWindowMs: Long = 5 * 60 * 1000L,
    // Минимальное и максимальное окно для поиска подпериода «вождения»
    val minWindowMs: Long = 60 * 1000L,
    val maxWindowMs: Long = 5 * 60 * 1000L,
    // Интервалы анализа (адаптивные)
    val initialAnalysisIntervalMs: Long = 60 * 1000L,
    val fastAnalysisIntervalMs: Long = 30 * 1000L,
    val lowAnalysisIntervalMs: Long = 2 * 60 * 1000L,
    val backgroundAnalysisIntervalMs: Long = 5 * 60 * 1000L,
    // Пороговые значения для смены интервалов по сериям результатов
    val drivingStreakForFast: Int = 3,
    val nonDrivingStreakForLow: Int = 5,
    val nonDrivingStreakForBackground: Int = 10,
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

    // Динамическое управление частотой анализа
    private var lastAnalysisTime: Long = 0L
    private var currentAnalysisIntervalMs: Long = initialAnalysisIntervalMs // по умолчанию анализ раз в минуту
    private var consecutiveDrivingCount: Int = 0
    private var consecutiveNonDrivingCount: Int = 0


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

            // Проверяем, нужно ли начать анализ. Анализ запускается
            // не чаще currentAnalysisIntervalMs и при наличии достаточных данных
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

        // Используем метки времени событий как источник истины времени анализа
        val lastEvent = motionHistory.last()
        val nowTs = lastEvent.timestamp

        // Троттлим анализ по интервалу
        // Для первого анализа (lastAnalysisTime == 0) пропускаем троттлинг
        if (lastAnalysisTime > 0L) {
            val sinceLastAnalysis = nowTs - lastAnalysisTime
            if (sinceLastAnalysis < currentAnalysisIntervalMs) {
                logger.debug(
                    LogCategory.LOCATION,
                    "$TAG: shouldStartAnalysis: throttled (${sinceLastAnalysis}ms < ${currentAnalysisIntervalMs}ms)"
                )
                return false
            }
        }

        // Убедимся, что есть хотя бы 2 события за последний retentionWindowMs
        val cutoffTime = nowTs - retentionWindowMs
        val recentEvents = motionHistory.asSequence().filter { it.timestamp >= cutoffTime }.toList()
        val result = recentEvents.size >= 2

        logger.debug(
            LogCategory.LOCATION,
            "$TAG: shouldStartAnalysis: size=${motionHistory.size}, recentEvents=${recentEvents.size}, cutoff=${cutoffTime}, now=${nowTs}, interval=${currentAnalysisIntervalMs}, result=$result"
        )
        return result
    }

    /**
     * Выполняет анализ накопленных данных
     */
    private suspend fun performAnalysis() {
        val nowTs = motionHistory.last().timestamp
        lastAnalysisTime = nowTs

        logger.debug(LogCategory.LOCATION, "$TAG: Starting analysis of ${motionHistory.size} events at $nowTs")

        // Берем историю только за retentionWindowMs
        val cutoff = nowTs - retentionWindowMs
        val recent = motionHistory.filter { it.timestamp >= cutoff }

        val drivingDetected = findDrivingWindow(
            events = recent,
            minWinMs = minWindowMs,
            maxWinMs = maxWindowMs,
            vehicleThreshold = vehicleTimeThreshold,
            confThreshold = confidenceThreshold
        )

        if (drivingDetected) {
            consecutiveDrivingCount += 1
            consecutiveNonDrivingCount = 0
            logger.info(LogCategory.LOCATION, "$TAG: Vehicle motion detected (consecutive=$consecutiveDrivingCount)")
            observeMotionTrigger.emit(Unit)
            // После триггера — полная очистка, чтобы исключить повторные срабатывания
            clear()
            // Ускоряем следующий анализ ненадолго (быстрый режим подтверждения)
            currentAnalysisIntervalMs = fastAnalysisIntervalMs
        } else {
            consecutiveNonDrivingCount += 1
            consecutiveDrivingCount = 0
            logger.debug(LogCategory.LOCATION, "$TAG: Not in vehicle, applying cleanup policies")
            // Чистим историю, гарантируя удержание последних 5 минут
            cleanupHistory(nowTs)
            // Увеличиваем интервал анализа для экономии батареи, если долго не едем
            updateAnalysisInterval()
        }
    }

    /**
     * Политика очистки истории: удерживаем максимум 5 минут данных и дополнительно
     * очищаем более старые записи агрессивнее при длительном отсутствии вождения.
     */
    private fun cleanupHistory(nowTs: Long) {
        if (motionHistory.isEmpty()) return
        val hardCutoff = nowTs - retentionWindowMs

        val before = motionHistory.size
        motionHistory.removeAll { it.timestamp < hardCutoff }
        val removed = before - motionHistory.size
        if (removed > 0) {
            logger.debug(LogCategory.LOCATION, "$TAG: cleanupHistory: removed=$removed, kept=${motionHistory.size}")
        }

        // Дополнительно: если долго не обнаруживаем вождение — слегка ужесточаем удержание
        if (consecutiveNonDrivingCount >= 5 && motionHistory.size > 2) {
            val strongerCutoff = nowTs - (retentionWindowMs - 2 * 60 * 1000L) // 5 мин -> 3 мин
            val before2 = motionHistory.size
            motionHistory.removeAll { it.timestamp < strongerCutoff }
            val removed2 = before2 - motionHistory.size
            if (removed2 > 0) {
                logger.debug(LogCategory.LOCATION, "$TAG: cleanupHistory (aggressive): removed=$removed2, kept=${motionHistory.size}")
            }
        }
    }

    /**
     * Адаптивное изменение интервала анализа для экономии батареи.
     */
    private fun updateAnalysisInterval() {
        currentAnalysisIntervalMs = when {
            consecutiveDrivingCount >= drivingStreakForFast -> fastAnalysisIntervalMs // активный режим подтверждения
            consecutiveNonDrivingCount >= nonDrivingStreakForBackground -> backgroundAnalysisIntervalMs // давно не едем — фоновый
            consecutiveNonDrivingCount >= nonDrivingStreakForLow -> lowAnalysisIntervalMs // низкая активность
            else -> initialAnalysisIntervalMs // обычный режим
        }
        logger.debug(
            LogCategory.LOCATION,
            "$TAG: updateAnalysisInterval -> ${currentAnalysisIntervalMs}ms (driveCnt=$consecutiveDrivingCount, nonDriveCnt=$consecutiveNonDrivingCount)"
        )
    }

    /**
     * Поиск любого подокна длительностью от minWinMs до maxWinMs, удовлетворяющего критериям «вождения».
     * Использует два указателя с инкрементальным учетом интервалов и взвешивания по confidence.
     */
    private fun findDrivingWindow(
        events: List<MotionAnalysisEvent>,
        minWinMs: Long,
        maxWinMs: Long,
        vehicleThreshold: Float,
        confThreshold: Int,
    ): Boolean {
        if (events.size < 2) return false

        data class Agg(
            var totalTime: Long = 0L,
            var vehicleTime: Long = 0L,
            var weightedConfidenceSum: Long = 0L,
            var timeSum: Long = 0L,
        )

        val agg = Agg()

        fun addInterval(i: Int) {
            val a = events[i]
            val b = events[i + 1]
            val dt = (b.timestamp - a.timestamp).coerceAtLeast(0L)
            agg.totalTime += dt
            // vehicleTime должен быть реальным временем в транспорте, а не взвешенным
            // Confidence учитывается отдельно при вычислении avgConf
            agg.vehicleTime += if (a.motionState == MotionState.IN_VEHICLE) dt else 0L
            agg.weightedConfidenceSum += a.confidence.toLong() * dt
            agg.timeSum += dt
        }

        fun removeInterval(i: Int) {
            val a = events[i]
            val b = events[i + 1]
            val dt = (b.timestamp - a.timestamp).coerceAtLeast(0L)
            agg.totalTime -= dt
            // vehicleTime должен быть реальным временем в транспорте, а не взвешенным
            agg.vehicleTime -= if (a.motionState == MotionState.IN_VEHICLE) dt else 0L
            agg.weightedConfidenceSum -= a.confidence.toLong() * dt
            agg.timeSum -= dt
        }

        var left = 0
        for (right in 1 until events.size) {
            addInterval(right - 1)

            // Сжимаем окно, если вышли за maxWinMs
            while (left < right - 1 && (events[right].timestamp - events[left].timestamp) > maxWinMs) {
                removeInterval(left)
                left++
            }

            val windowMs = events[right].timestamp - events[left].timestamp
            if (windowMs >= minWinMs && agg.totalTime > 0L && agg.timeSum > 0L) {
                val vehiclePct = agg.vehicleTime.toFloat() / agg.totalTime
                val avgConf = (agg.weightedConfidenceSum / agg.timeSum).toInt()
                logger.debug(
                    LogCategory.LOCATION,
                    "$TAG: window [$left,$right] ms=$windowMs vehiclePct=${(vehiclePct * 100).toInt()}% avgConf=$avgConf%"
                )
                if (vehiclePct >= vehicleThreshold && avgConf >= confThreshold) return true
            }
        }
        return false
    }
}
