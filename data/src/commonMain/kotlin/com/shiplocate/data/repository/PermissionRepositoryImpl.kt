package com.shiplocate.data.repository

import com.shiplocate.data.datasource.PermissionDataSource
import com.shiplocate.data.mapper.PermissionMapper
import com.shiplocate.domain.model.PermissionStatus
import com.shiplocate.domain.repository.PermissionRepository

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
}
