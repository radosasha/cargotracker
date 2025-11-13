package com.shiplocate.data.datasource

/**
 * Интерфейс для запроса разрешений
 * Находится в data слое, так как это деталь реализации (DataSource)
 */
interface PermissionManager {
    suspend fun hasLocationPermissions(): Boolean

    suspend fun hasBackgroundLocationPermission(): Boolean

    suspend fun hasNotificationPermission(): Boolean

    suspend fun hasActivityRecognitionPermission(): Boolean

    suspend fun isBatteryOptimizationDisabled(): Boolean

    suspend fun requestAllPermissions(): Result<Unit>

    suspend fun requestNotificationPermission(): Result<Unit>

    /**
     * Запрашивает разрешение на точное местоположение (Precise Location)
     */
    suspend fun requestLocationPermission(): Result<Unit>

    /**
     * Запрашивает разрешение на фоновое местоположение (Background Location)
     */
    suspend fun requestBackgroundLocationPermission(): Result<Unit>

    /**
     * Запрашивает отключение оптимизации батареи (Unrestricted Battery Use)
     */
    suspend fun requestBatteryOptimizationDisable(): Result<Unit>
}
