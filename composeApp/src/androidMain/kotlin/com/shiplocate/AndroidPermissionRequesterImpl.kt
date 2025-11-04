package com.shiplocate

import com.shiplocate.data.datasource.PermissionRequester
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android реализация PermissionRequester
 */
class AndroidPermissionRequesterImpl : PermissionRequester, KoinComponent {
    private val activityContextProvider: ActivityProvider by inject()
    
    // Lazy инициализация - создается только при первом обращении
    private val permissionRequester: AndroidPermissionRequester by lazy {
        AndroidPermissionRequester(activityContextProvider.getActivity())
    }

    override suspend fun hasLocationPermissions(): Boolean {
        return permissionRequester.hasLocationPermissions()
    }

    override suspend fun hasBackgroundLocationPermission(): Boolean {
        return permissionRequester.hasBackgroundLocationPermission()
    }

    override suspend fun hasNotificationPermission(): Boolean {
        return permissionRequester.hasNotificationPermission()
    }

    override suspend fun hasActivityRecognitionPermission(): Boolean {
        return permissionRequester.hasActivityRecognitionPermission()
    }

    override suspend fun isBatteryOptimizationDisabled(): Boolean {
        return permissionRequester.isBatteryOptimizationDisabled()
    }

    override suspend fun requestAllPermissions(): Result<Unit> {
        return try {
            permissionRequester.requestAllPermissions()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestNotificationPermission(): Result<Unit> {
        return try {
            permissionRequester.requestNotificationPermission()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
