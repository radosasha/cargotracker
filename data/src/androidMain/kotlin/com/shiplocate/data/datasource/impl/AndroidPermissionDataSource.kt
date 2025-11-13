package com.shiplocate.data.datasource.impl

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.PermissionDataSource
import com.shiplocate.data.datasource.PermissionManager
import com.shiplocate.data.model.PermissionDataModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Android реализация PermissionDataSource
 */
class AndroidPermissionDataSource(
    private val permissionManager: PermissionManager,
    private val logger: Logger,
) : PermissionDataSource {
    // Flow для уведомлений о получении разрешений
    private val permissionsFlow = MutableSharedFlow<PermissionDataModel>(replay = 0)
    
    // Coroutine scope для эмита в Flow
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
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

    override suspend fun requestLocationPermission(): Result<PermissionDataModel> {
        return try {
            logger.debug(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.requestLocationPermission() called")
            permissionManager.requestLocationPermission()

            val status = getPermissionStatus()
            logger.debug(
                LogCategory.PERMISSIONS,
                "AndroidPermissionDataSource.requestLocationPermission() - status: location=${status.hasLocationPermission}",
            )

            Result.success(status)
        } catch (e: Exception) {
            logger.error(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.requestLocationPermission() - exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun requestBackgroundLocationPermission(): Result<PermissionDataModel> {
        return try {
            logger.debug(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.requestBackgroundLocationPermission() called")
            permissionManager.requestBackgroundLocationPermission()

            val status = getPermissionStatus()
            logger.debug(
                LogCategory.PERMISSIONS,
                "AndroidPermissionDataSource.requestBackgroundLocationPermission() - status: background=${status.hasBackgroundLocationPermission}",
            )

            Result.success(status)
        } catch (e: Exception) {
            logger.error(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.requestBackgroundLocationPermission() - exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun requestBatteryOptimizationDisable(): Result<PermissionDataModel> {
        return try {
            logger.debug(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.requestBatteryOptimizationDisable() called")
            permissionManager.requestBatteryOptimizationDisable()

            val status = getPermissionStatus()
            logger.debug(
                LogCategory.PERMISSIONS,
                "AndroidPermissionDataSource.requestBatteryOptimizationDisable() - status: battery=${status.isBatteryOptimizationDisabled}",
            )

            Result.success(status)
        } catch (e: Exception) {
            logger.error(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.requestBatteryOptimizationDisable() - exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun notifyPermissionGranted() {
        logger.debug(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.notifyPermissionGranted() called")
        val status = getPermissionStatus()
        scope.launch {
            permissionsFlow.emit(status)
            logger.debug(LogCategory.PERMISSIONS, "AndroidPermissionDataSource: Emitted permission status to flow")
        }
    }

    override fun observePermissions(): Flow<PermissionDataModel> {
        logger.debug(LogCategory.PERMISSIONS, "AndroidPermissionDataSource.observePermissions() called")
        return permissionsFlow.asSharedFlow()
    }
}
