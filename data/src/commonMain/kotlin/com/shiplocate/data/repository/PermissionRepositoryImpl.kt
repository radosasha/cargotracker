package com.shiplocate.data.repository

import com.shiplocate.data.datasource.PermissionDataSource
import com.shiplocate.data.mapper.PermissionMapper
import com.shiplocate.domain.model.PermissionStatus
import com.shiplocate.domain.repository.PermissionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Реализация PermissionRepository
 */
class PermissionRepositoryImpl(
    private val permissionDataSource: PermissionDataSource,
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

    override suspend fun requestNotificationPermission(): Result<Boolean> {
        return permissionDataSource.requestNotificationPermission()
    }

    override suspend fun requestLocationPermission(): Result<PermissionStatus> {
        return permissionDataSource.requestLocationPermission().map { dataModel ->
            PermissionMapper.toDomain(dataModel)
        }
    }

    override suspend fun requestBackgroundLocationPermission(): Result<PermissionStatus> {
        return permissionDataSource.requestBackgroundLocationPermission().map { dataModel ->
            PermissionMapper.toDomain(dataModel)
        }
    }

    override suspend fun requestBatteryOptimizationDisable(): Result<PermissionStatus> {
        return permissionDataSource.requestBatteryOptimizationDisable().map { dataModel ->
            PermissionMapper.toDomain(dataModel)
        }
    }

    override suspend fun notifyPermissionGranted() {
        permissionDataSource.notifyPermissionGranted()
    }

    override fun observePermissions(): Flow<PermissionStatus> {
        return permissionDataSource.observePermissions().map { dataModel ->
            PermissionMapper.toDomain(dataModel)
        }
    }
}
