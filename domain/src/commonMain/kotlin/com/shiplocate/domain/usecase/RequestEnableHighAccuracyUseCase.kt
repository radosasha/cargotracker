package com.shiplocate.domain.usecase

import com.shiplocate.domain.model.PermissionStatus
import com.shiplocate.domain.repository.PermissionRepository

class RequestEnableHighAccuracyUseCase(
    private val permissionRepository: PermissionRepository,
) {
    suspend operator fun invoke(): Result<PermissionStatus> {
        return permissionRepository.requestEnableHighAccuracy()
    }
}

