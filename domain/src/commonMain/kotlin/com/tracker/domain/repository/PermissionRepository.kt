package com.tracker.domain.repository

import com.tracker.domain.model.PermissionStatus

/**
 * Интерфейс репозитория для работы с разрешениями
 */
interface PermissionRepository {
    /**
     * Получает текущий статус разрешений
     */
    suspend fun getPermissionStatus(): PermissionStatus

    /**
     * Запрашивает все необходимые разрешения
     */
    suspend fun requestAllPermissions(): Result<PermissionStatus>

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
