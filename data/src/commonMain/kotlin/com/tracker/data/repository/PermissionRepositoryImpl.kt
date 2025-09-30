package com.tracker.data.repository

import com.tracker.data.datasource.PermissionDataSource
import com.tracker.data.mapper.PermissionMapper
import com.tracker.domain.model.PermissionStatus
import com.tracker.domain.repository.PermissionRepository

/**
 * Реализация PermissionRepository
 */
class PermissionRepositoryImpl(
    private val permissionDataSource: PermissionDataSource
) : PermissionRepository {
    
    override suspend fun getPermissionStatus(): PermissionStatus {
        val dataModel = permissionDataSource.getPermissionStatus()
        return PermissionMapper.toDomain(dataModel)
    }
    
    override suspend fun requestAllPermissions(): Result<PermissionStatus> {
        return permissionDataSource.requestAllPermissions().map { dataModel ->
            PermissionMapper.toDomain(dataModel)
        }
    }
    
    override suspend fun requestLocationPermissions(): Result<Boolean> {
        return permissionDataSource.requestLocationPermissions()
    }
    
    override suspend fun requestBackgroundLocationPermission(): Result<Boolean> {
        return permissionDataSource.requestBackgroundLocationPermission()
    }
    
    override suspend fun requestNotificationPermission(): Result<Boolean> {
        return permissionDataSource.requestNotificationPermission()
    }
    
    override suspend fun openAppSettings() {
        permissionDataSource.openAppSettings()
    }
    
    override suspend fun requestBatteryOptimizationDisable(): Result<Boolean> {
        return permissionDataSource.requestBatteryOptimizationDisable()
    }
}
