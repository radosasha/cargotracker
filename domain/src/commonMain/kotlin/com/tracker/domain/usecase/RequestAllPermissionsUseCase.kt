package com.tracker.domain.usecase

import com.tracker.domain.model.PermissionStatus
import com.tracker.domain.repository.PermissionRepository

/**
 * Use Case для запроса всех необходимых разрешений
 */
class RequestAllPermissionsUseCase(
    private val permissionRepository: PermissionRepository,
) {
    suspend operator fun invoke(): Result<PermissionStatus> {
        return permissionRepository.requestAllPermissions()
    }
}
