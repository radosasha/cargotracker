package com.shiplocate.domain.model

import kotlinx.datetime.Instant

/**
 * Domain модель для GPS координат
 */
data class GpsLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double? = null,
    val speed: Float? = null,
    val bearing: Float? = null,
    val timestamp: Instant,
)

/**
 * Статус GPS трекинга
 */
enum class TrackingStatus {
    STOPPED,
    STARTING,
    ACTIVE,
    STOPPING,
    ERROR,
}

/**
 * Статус разрешений
 */
data class PermissionStatus(
    val hasLocationPermission: Boolean,
    val hasBackgroundLocationPermission: Boolean,
    val hasNotificationPermission: Boolean,
//    val hasActivityRecognitionPermission: Boolean,
    val isBatteryOptimizationDisabled: Boolean,
) {
    val hasAllPermissions: Boolean
//        get() = hasLocationPermission && hasBackgroundLocationPermission && hasNotificationPermission && hasActivityRecognitionPermission && isBatteryOptimizationDisabled
        get() = hasLocationPermission && hasBackgroundLocationPermission && hasNotificationPermission && isBatteryOptimizationDisabled
}
