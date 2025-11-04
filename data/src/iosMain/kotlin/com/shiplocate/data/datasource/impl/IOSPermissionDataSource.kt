package com.shiplocate.data.datasource.impl

import com.shiplocate.data.datasource.PermissionDataSource
import com.shiplocate.data.datasource.PermissionRequester
import com.shiplocate.data.model.PermissionDataModel

/**
 * iOS реализация PermissionDataSource
 */
class IOSPermissionDataSource(
    private val permissionRequester: PermissionRequester,
) : PermissionDataSource {
    override suspend fun getPermissionStatus(): PermissionDataModel {
        return PermissionDataModel(
            hasLocationPermission = permissionRequester.hasLocationPermissions(),
            hasBackgroundLocationPermission = permissionRequester.hasBackgroundLocationPermission(),
            hasNotificationPermission = permissionRequester.hasNotificationPermission(),
            hasActivityRecognitionPermission = permissionRequester.hasActivityRecognitionPermission(),
            isBatteryOptimizationDisabled = permissionRequester.isBatteryOptimizationDisabled(),
        )
    }

    override suspend fun requestAllPermissions(): Result<PermissionDataModel> {
        return try {
            // Используем suspend версию, которая ждет результата через callbacks
            val result = permissionRequester.requestAllPermissions()
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
            val result = permissionRequester.requestNotificationPermission()
            if (result.isFailure) {
                return Result.failure(result.exceptionOrNull() ?: Exception("Failed to request notification permission"))
            }
            Result.success(permissionRequester.hasNotificationPermission())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
