package com.shiplocate.domain.usecase

import com.shiplocate.domain.model.PermissionStatus
import com.shiplocate.domain.repository.PermissionRepository

/**
 * Use Case для получения статуса разрешений
 */
class GetPermissionStatusUseCase(
    private val permissionRepository: PermissionRepository,
) {
    suspend operator fun invoke(): PermissionStatus {
        return permissionRepository.getPermissionStatus()
    }
}
