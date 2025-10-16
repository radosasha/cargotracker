package com.tracker.domain.usecase

import com.tracker.domain.model.PermissionStatus
import com.tracker.domain.repository.PermissionRepository

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
