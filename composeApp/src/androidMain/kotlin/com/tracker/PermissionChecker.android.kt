package com.tracker

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android реализация проверки разрешений
 */
actual class PermissionChecker : KoinComponent {
    private val activityContextProvider: ActivityProvider by inject()
    private val permissionRequester = AndroidPermissionRequester(activityContextProvider.getActivity())

    actual suspend fun hasLocationPermissions(): Boolean {
        return permissionRequester.hasLocationPermissions()
    }

    actual suspend fun hasBackgroundLocationPermission(): Boolean {
        return permissionRequester.hasBackgroundLocationPermission()
    }

    actual suspend fun hasNotificationPermission(): Boolean {
        return permissionRequester.hasNotificationPermission()
    }

    actual suspend fun hasAllRequiredPermissions(): Boolean {
        val result = permissionRequester.hasAllRequiredPermissions()
        println("PermissionChecker.hasAllRequiredPermissions(): $result")
        return result
    }

    actual suspend fun getPermissionStatusMessage(): String {
        val message = permissionRequester.getPermissionStatusMessage()
        println("PermissionChecker.getPermissionStatusMessage(): $message")
        return message
    }

    actual suspend fun openAppSettings(): Result<Unit> {
        return try {
            permissionRequester.openAppSettings()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual fun requestAllPermissions() {
        println("PermissionChecker.requestAllPermissions() called")
        try {
            permissionRequester.requestAllPermissions()
            println("PermissionRequester.requestAllPermissions() completed")
        } catch (e: Exception) {
            println("Error in PermissionChecker.requestAllPermissions(): ${e.message}")
            throw e
        }
    }
}
