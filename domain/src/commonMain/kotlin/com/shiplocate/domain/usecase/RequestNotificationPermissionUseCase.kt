package com.shiplocate.domain.usecase

import com.shiplocate.domain.repository.PermissionRepository

/**
 * Use Case для запроса разрешений на уведомления
 */
class RequestNotificationPermissionUseCase(
    private val permissionRepository: PermissionRepository,
) {
    suspend operator fun invoke(): Result<Boolean> {
        return permissionRepository.requestNotificationPermission()
    }
}
