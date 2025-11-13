package com.shiplocate.domain.usecase

import com.shiplocate.domain.model.PermissionStatus
import com.shiplocate.domain.repository.PermissionRepository

/**
 * Use case для запроса разрешения на фоновое местоположение (Background Location)
 */
class RequestBackgroundLocationPermissionUseCase(
    private val permissionRepository: PermissionRepository,
) {
    suspend operator fun invoke(): Result<PermissionStatus> {
        return permissionRepository.requestBackgroundLocationPermission()
    }
}

