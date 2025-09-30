package com.tracker.domain.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

/**
 * Domain модель для GPS координат
 */
@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double? = null,
    val speed: Float? = null,
    val bearing: Float? = null,
    val timestamp: Instant,
    val deviceId: String? = null
)

/**
 * Результат операции с GPS данными
 */
sealed class LocationResult {
    data class Success(val location: Location) : LocationResult()
    data class Error(val exception: Throwable) : LocationResult()
}

/**
 * Статус GPS трекинга
 */
enum class TrackingStatus {
    STOPPED,
    STARTING,
    ACTIVE,
    STOPPING,
    ERROR
}

/**
 * Статус разрешений
 */
data class PermissionStatus(
    val hasLocationPermission: Boolean,
    val hasBackgroundLocationPermission: Boolean,
    val hasNotificationPermission: Boolean,
    val isBatteryOptimizationDisabled: Boolean
) {
    val hasAllPermissions: Boolean
        get() = hasLocationPermission && hasBackgroundLocationPermission && hasNotificationPermission && isBatteryOptimizationDisabled
}
