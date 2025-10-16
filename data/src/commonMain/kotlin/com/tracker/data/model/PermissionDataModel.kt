package com.tracker.data.model

/**
 * Data модель для статуса разрешений
 */
data class PermissionDataModel(
    val hasLocationPermission: Boolean,
    val hasBackgroundLocationPermission: Boolean,
    val hasNotificationPermission: Boolean,
    val isBatteryOptimizationDisabled: Boolean,
)
