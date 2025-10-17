package com.tracker

/**
 * Общий интерфейс для проверки разрешений
 */
expect class PermissionChecker() {
    suspend fun hasLocationPermissions(): Boolean

    suspend fun hasBackgroundLocationPermission(): Boolean

    suspend fun hasNotificationPermission(): Boolean

    suspend fun hasAllRequiredPermissions(): Boolean

    suspend fun getPermissionStatusMessage(): String

    suspend fun openAppSettings(): Result<Unit>

    fun requestAllPermissions()
}
