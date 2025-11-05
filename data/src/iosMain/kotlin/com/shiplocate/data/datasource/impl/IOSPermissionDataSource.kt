package com.shiplocate.data.datasource.impl

import com.shiplocate.data.datasource.PermissionDataSource
import com.shiplocate.data.datasource.PermissionManager
import com.shiplocate.data.model.PermissionDataModel

/**
 * iOS реализация PermissionDataSource
 */
class IOSPermissionDataSource(
    private val permissionManager: PermissionManager,
) : PermissionDataSource {
    override suspend fun getPermissionStatus(): PermissionDataModel {
        return PermissionDataModel(
            hasLocationPermission = permissionManager.hasLocationPermissions(),
            hasBackgroundLocationPermission = permissionManager.hasBackgroundLocationPermission(),
            hasNotificationPermission = permissionManager.hasNotificationPermission(),
            hasActivityRecognitionPermission = permissionManager.hasActivityRecognitionPermission(),
            isBatteryOptimizationDisabled = permissionManager.isBatteryOptimizationDisabled(),
        )
    }

    override suspend fun requestAllPermissions(): Result<PermissionDataModel> {
        return try {
            // Используем suspend версию, которая ждет результата через callbacks
            val result = permissionManager.requestAllPermissions()
            if (result.isFailure) {
                return Result.failure(result.exceptionOrNull() ?: Exception("Failed to request permissions"))
            }

            // Получаем финальный статус разрешений
            val status = getPermissionStatus()

            if (status.hasLocationPermission && status.hasBackgroundLocationPermission && status.hasNotificationPermission && status.hasActivityRecognitionPermission) {
                Result.success(status)
            } else {
                Result.failure(Exception("Не все разрешения получены"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestNotificationPermission(): Result<Boolean> {
        return try {
            val result = permissionManager.requestNotificationPermission()
            if (result.isFailure) {
                return Result.failure(result.exceptionOrNull() ?: Exception("Failed to request notification permission"))
            }
            Result.success(permissionManager.hasNotificationPermission())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
