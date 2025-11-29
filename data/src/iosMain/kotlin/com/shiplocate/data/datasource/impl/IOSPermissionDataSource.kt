package com.shiplocate.data.datasource.impl

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
 * iOS реализация PermissionDataSource
 */
class IOSPermissionDataSource(
    private val permissionManager: PermissionManager,
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
            isBatteryOptimizationDisabled = permissionManager.isBatteryOptimizationDisabled(),
            isHighAccuracyEnabled = true
        )
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

    override suspend fun requestLocationPermission(): Result<PermissionDataModel> {
        return try {
            val result = permissionManager.requestLocationPermission()
            if (result.isFailure) {
                return Result.failure(result.exceptionOrNull() ?: Exception("Failed to request location permission"))
            }
            val status = getPermissionStatus()
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestBackgroundLocationPermission(): Result<PermissionDataModel> {
        return try {
            val result = permissionManager.requestBackgroundLocationPermission()
            if (result.isFailure) {
                return Result.failure(result.exceptionOrNull() ?: Exception("Failed to request background location permission"))
            }
            val status = getPermissionStatus()
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestBatteryOptimizationDisable(): Result<PermissionDataModel> {
        return try {
            // iOS не требует отдельного разрешения для батареи
            // Возвращаем текущий статус
            val status = getPermissionStatus()
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestEnableHighAccuracy(): Result<PermissionDataModel> {
        return try {
            permissionManager.requestEnableHighAccuracy()
            val status = getPermissionStatus()
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun notifyPermissionGranted() {
        val status = getPermissionStatus()
        scope.launch {
            permissionsFlow.emit(status)
        }
    }

    override fun observePermissions(): Flow<PermissionDataModel> {
        return permissionsFlow.asSharedFlow()
    }
}
