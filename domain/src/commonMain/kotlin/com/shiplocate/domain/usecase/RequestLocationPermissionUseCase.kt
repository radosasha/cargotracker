package com.shiplocate.domain.usecase

import com.shiplocate.domain.model.PermissionStatus
import com.shiplocate.domain.repository.PermissionRepository

/**
 * Use case для запроса разрешения на точное местоположение (Precise Location)
 */
class RequestLocationPermissionUseCase(
    private val permissionRepository: PermissionRepository,
) {
    suspend operator fun invoke(): Result<PermissionStatus> {
        return permissionRepository.requestLocationPermission()
    }
}

