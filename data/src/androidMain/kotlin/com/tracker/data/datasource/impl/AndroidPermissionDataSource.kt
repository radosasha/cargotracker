package com.tracker.data.datasource.impl

import com.tracker.data.datasource.PermissionDataSource
import com.tracker.data.model.PermissionDataModel
import com.tracker.domain.datasource.PermissionRequester
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android реализация PermissionDataSource
 */
class AndroidPermissionDataSource : PermissionDataSource, KoinComponent {
    
    private val permissionRequester: PermissionRequester by inject()
    
    override suspend fun getPermissionStatus(): PermissionDataModel {
        return PermissionDataModel(
            hasLocationPermission = permissionRequester.hasLocationPermissions(),
            hasBackgroundLocationPermission = permissionRequester.hasBackgroundLocationPermission(),
            hasNotificationPermission = permissionRequester.hasNotificationPermission(),
            isBatteryOptimizationDisabled = permissionRequester.isBatteryOptimizationDisabled()
        )
    }
    
    override suspend fun requestAllPermissions(): Result<PermissionDataModel> {
        return try {
            println("AndroidPermissionDataSource.requestAllPermissions() called")
            permissionRequester.requestAllPermissions()
            
            val status = getPermissionStatus()
            println("AndroidPermissionDataSource.requestAllPermissions() - status: location=${status.hasLocationPermission}, background=${status.hasBackgroundLocationPermission}, notification=${status.hasNotificationPermission}, battery=${status.isBatteryOptimizationDisabled}")
            
            if (status.hasLocationPermission && status.hasBackgroundLocationPermission && status.hasNotificationPermission && status.isBatteryOptimizationDisabled) {
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
            permissionRequester.requestAllPermissions()
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
