package com.shiplocate.data.datasource

/**
 * Интерфейс для проверки и запроса разрешений
 */
interface PermissionChecker {
    /**
     * Проверяет, есть ли разрешение на местоположение
     */
    suspend fun hasLocationPermissions(): Boolean

    /**
     * Проверяет, есть ли разрешение на фоновое местоположение
     */
    suspend fun hasBackgroundLocationPermission(): Boolean

    /**
     * Проверяет, есть ли разрешение на уведомления
     */
    suspend fun hasNotificationPermission(): Boolean

    /**
     * Проверяет, есть ли все необходимые разрешения
     */
    suspend fun hasAllRequiredPermissions(): Boolean

    /**
     * Получает сообщение о статусе разрешений
     */
    suspend fun getPermissionStatusMessage(): String

    /**
     * Открывает настройки приложения
     */
    suspend fun openAppSettings(): Result<Unit>

    /**
     * Запрашивает все необходимые разрешения
     */
    fun requestAllPermissions()
}
