package com.shiplocate.trackingsdk.motion.models

/**
 * Событие движения от ActivityRecognition
 */
data class MotionEvent(
    val motionState: MotionState,
    val confidence: Int, // Уверенность в процентах (0-100)
    val timestamp: Long
)

/**
 * Состояния движения пользователя
 */
enum class MotionState {
    STATIONARY,     // Неподвижен
    WALKING,        // Идет пешком
    RUNNING,        // Бежит
    IN_VEHICLE,     // В транспорте
    ON_BICYCLE,     // На велосипеде
    UNKNOWN         // Неизвестно
}

/**
 * Результат анализа движения
 */
data class MotionAnalysisEvent(
    val motionState: MotionState,
    val confidence: Int, // Уверенность в процентах (0-100)
    val timestamp: Long,
    val isInVehicle: Boolean = motionState == MotionState.IN_VEHICLE
)

/**
 * Статистика движения за период
 */
data class MotionStatistics(
    val totalTimeMs: Long,
    val vehicleTimeMs: Long,
    val walkingTimeMs: Long,
    val stationaryTimeMs: Long,
    val vehiclePercentage: Float,
    val lastActivity: MotionState,
    val confidence: Int
)
