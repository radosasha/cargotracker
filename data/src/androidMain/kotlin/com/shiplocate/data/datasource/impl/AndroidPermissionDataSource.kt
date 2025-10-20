package com.shiplocate.data.datasource.impl

import com.shiplocate.data.datasource.PermissionDataSource
import com.shiplocate.data.datasource.PermissionRequester
import com.shiplocate.data.model.PermissionDataModel

/**
 * Android реализация PermissionDataSource
 */
class AndroidPermissionDataSource(
    private val permissionRequester: PermissionRequester,
) : PermissionDataSource {
    override suspend fun getPermissionStatus(): PermissionDataModel {
        return PermissionDataModel(
            hasLocationPermission = permissionRequester.hasLocationPermissions(),
            hasBackgroundLocationPermission = permissionRequester.hasBackgroundLocationPermission(),
            hasNotificationPermission = permissionRequester.hasNotificationPermission(),
            isBatteryOptimizationDisabled = permissionRequester.isBatteryOptimizationDisabled(),
        )
    }

    override suspend fun requestAllPermissions(): Result<PermissionDataModel> {
        return try {
            println("AndroidPermissionDataSource.requestAllPermissions() called")
            permissionRequester.requestAllPermissions()

            val status = getPermissionStatus()
            println(
                "AndroidPermissionDataSource.requestAllPermissions() - status: location=${status.hasLocationPermission}, " +
                    "background=${status.hasBackgroundLocationPermission}, notification=${status.hasNotificationPermission}, " +
                    "battery=${status.isBatteryOptimizationDisabled}",
            )

            if (
                status.hasLocationPermission &&
                status.hasBackgroundLocationPermission &&
                status.hasNotificationPermission &&
                status.isBatteryOptimizationDisabled
            ) {
                println("AndroidPermissionDataSource.requestAllPermissions() - all permissions granted, returning success")
                Result.success(status)
            } else {
                println("AndroidPermissionDataSource.requestAllPermissions() - not all permissions granted, returning failure")
                Result.failure(Exception("Не все разрешения получены"))
            }
        } catch (e: Exception) {
            println("AndroidPermissionDataSource.requestAllPermissions() - exception: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun requestLocationPermissions(): Result<Boolean> {
        return try {
            permissionRequester.requestAllPermissions()
            Result.success(permissionRequester.hasLocationPermissions())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestBackgroundLocationPermission(): Result<Boolean> {
        return try {
            permissionRequester.requestAllPermissions()
            Result.success(permissionRequester.hasBackgroundLocationPermission())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestNotificationPermission(): Result<Boolean> {
        return try {
            permissionRequester.requestNotificationPermission()
            Result.success(permissionRequester.hasNotificationPermission())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun openAppSettings() {
        permissionRequester.openAppSettings()
    }

    override suspend fun requestBatteryOptimizationDisable(): Result<Boolean> {
        return try {
            permissionRequester.openAppSettings()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
