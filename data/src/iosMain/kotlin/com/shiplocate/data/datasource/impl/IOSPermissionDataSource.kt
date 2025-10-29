package com.shiplocate.data.datasource.impl

import com.shiplocate.data.datasource.PermissionChecker
import com.shiplocate.data.datasource.PermissionDataSource
import com.shiplocate.data.model.PermissionDataModel
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

/**
 * iOS реализация PermissionDataSource
 */
class IOSPermissionDataSource(
    private val permissionChecker: PermissionChecker,
) : PermissionDataSource {
    override suspend fun getPermissionStatus(): PermissionDataModel {
        return PermissionDataModel(
            hasLocationPermission = permissionChecker.hasLocationPermissions(),
            hasBackgroundLocationPermission = permissionChecker.hasBackgroundLocationPermission(),
            hasNotificationPermission = permissionChecker.hasNotificationPermission(),
            hasActivityRecognitionPermission = permissionChecker.hasActivityRecognitionPermission(),
            isBatteryOptimizationDisabled = true, // В iOS нет понятия оптимизации батареи
        )
    }

    override suspend fun requestAllPermissions(): Result<PermissionDataModel> {
        return try {
            // Запрашиваем все разрешения
            permissionChecker.requestAllPermissions()

            // Ждем немного, чтобы разрешения успели обновиться
            kotlinx.coroutines.delay(1000)

            val status = getPermissionStatus()
            println("IOSPermissionDataSource: Permission status after request: $status")

            if (status.hasLocationPermission && status.hasBackgroundLocationPermission && status.hasNotificationPermission && status.hasActivityRecognitionPermission) {
                Result.success(status)
            } else {
                Result.failure(Exception("Не все разрешения получены"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestLocationPermissions(): Result<Boolean> {
        return try {
            permissionChecker.requestAllPermissions()
            kotlinx.coroutines.delay(1000)
            Result.success(permissionChecker.hasLocationPermissions())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestBackgroundLocationPermission(): Result<Boolean> {
        return try {
            permissionChecker.requestAllPermissions()
            kotlinx.coroutines.delay(1000)
            Result.success(permissionChecker.hasBackgroundLocationPermission())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestNotificationPermission(): Result<Boolean> {
        return try {
            permissionChecker.requestNotificationPermission()
            kotlinx.coroutines.delay(1000)
            Result.success(permissionChecker.hasNotificationPermission())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun openAppSettings() {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        settingsUrl?.let { url ->
            UIApplication.sharedApplication.openURL(url)
        }
    }

    override suspend fun requestBatteryOptimizationDisable(): Result<Boolean> {
        // В iOS не применимо
        return Result.success(true)
    }
}
