package com.shiplocate.trackingsdk.motion.models

/**
 * Событие от MotionTracker
 */
sealed class MotionTrackerEvent {
    /**
     * Обнаружено движение в транспорте - триггер для переключения в TRIP_RECORDING
     */
    object InVehicle : MotionTrackerEvent()
    
    /**
     * Результаты анализа движения (даже если вождение не обнаружено)
     */
    data class CheckingMotion(
        val statistics: MotionAnalysisResult,
        val timestamp: Long,
    ) : MotionTrackerEvent()
}

/**
 * Результаты анализа движения для передачи в trackingState
 */
data class MotionAnalysisResult(
    val drivingDetected: Boolean,
    val statistics: MotionStatistics? = null, // Статистика за период анализа
    val vehicleTimePercentage: Float = 0f, // Процент времени в транспорте (0-1)
    val averageConfidence: Int = 0, // Средняя уверенность
    val eventsAnalyzed: Int = 0, // Количество событий, проанализированных
    val analysisWindowMs: Long = 0L, // Длительность окна анализа
    val consecutiveDrivingCount: Int = 0, // Последовательные обнаружения вождения
    val consecutiveNonDrivingCount: Int = 0, // Последовательные отсутствия вождения
)

/**
 * Детальная статистика движения за период анализа
 */
data class MotionStatistics(
    val totalTimeMs: Long, // Общее время анализа
    val vehicleTimeMs: Long, // Время в транспорте
    val walkingTimeMs: Long, // Время пешком
    val stationaryTimeMs: Long, // Время неподвижно
    val vehiclePercentage: Float, // Процент времени в транспорте (0-1)
    val lastActivity: MotionState, // Последнее активное состояние
    val confidence: Int, // Средневзвешенная уверенность
)

