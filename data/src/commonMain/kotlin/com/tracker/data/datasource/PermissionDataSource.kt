package com.tracker.data.datasource

import com.tracker.data.model.PermissionDataModel

/**
 * Data Source для работы с разрешениями
 */
interface PermissionDataSource {
    
    /**
     * Получает текущий статус разрешений
     */
    suspend fun getPermissionStatus(): PermissionDataModel
    
    /**
     * Запрашивает все необходимые разрешения
     */
    suspend fun requestAllPermissions(): Result<PermissionDataModel>
    
    /**
     * Запрашивает разрешения на GPS
     */
    suspend fun requestLocationPermissions(): Result<Boolean>
    
    /**
     * Запрашивает разрешения на фоновое отслеживание
     */
    suspend fun requestBackgroundLocationPermission(): Result<Boolean>
    
    /**
     * Запрашивает разрешения на уведомления
     */
    suspend fun requestNotificationPermission(): Result<Boolean>
    
    /**
     * Открывает настройки приложения
     */
    suspend fun openAppSettings()
    
    /**
     * Запрашивает отключение оптимизации батареи
     */
    suspend fun requestBatteryOptimizationDisable(): Result<Boolean>
}
