package com.tracker.domain.datasource

/**
 * Интерфейс для запроса разрешений
 */
interface PermissionRequester {
    suspend fun hasLocationPermissions(): Boolean
    suspend fun hasBackgroundLocationPermission(): Boolean
    suspend fun hasNotificationPermission(): Boolean
    suspend fun isBatteryOptimizationDisabled(): Boolean
    suspend fun requestAllPermissions(): Result<Unit>
    suspend fun openAppSettings(): Result<Unit>
}
