package com.shiplocate.data.datasource.impl

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.PermissionDataSource
import com.shiplocate.data.datasource.PermissionManager
import com.shiplocate.data.model.PermissionDataModel

/**
 * Android реализация PermissionDataSource
 */
class AndroidPermissionDataSource(
    private val permissionManager: PermissionManager,
    private val logger: Logger,
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
            logger.debug(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.requestAllPermissions() called")
            permissionManager.requestAllPermissions()

            val status = getPermissionStatus()
            logger.debug(
                LogCategory.PERMISSIONS,
                "AndroidPermissionDataSource.requestAllPermissions() - status: location=${status.hasLocationPermission}, " +
                    "background=${status.hasBackgroundLocationPermission}, notification=${status.hasNotificationPermission}, " +
                    "activityRecognition=${status.hasActivityRecognitionPermission}, battery=${status.isBatteryOptimizationDisabled}",
            )

            if (
                status.hasLocationPermission &&
                status.hasBackgroundLocationPermission &&
                status.hasNotificationPermission &&
                status.hasActivityRecognitionPermission &&
                status.isBatteryOptimizationDisabled
            ) {
                logger.info(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.requestAllPermissions() - all permissions granted, returning success")
                Result.success(status)
            } else {
                logger.warn(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.requestAllPermissions() - not all permissions granted, returning failure")
                Result.failure(Exception("Не все разрешения получены"))
            }
        } catch (e: Exception) {
            logger.error(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.requestAllPermissions() - exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun requestNotificationPermission(): Result<Boolean> {
        return try {
            permissionManager.requestNotificationPermission()
            Result.success(permissionManager.hasNotificationPermission())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
