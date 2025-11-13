package com.shiplocate.domain.usecase

import com.shiplocate.domain.model.PermissionStatus
import com.shiplocate.domain.repository.PermissionRepository

/**
 * Use case для запроса отключения оптимизации батареи (Unrestricted Battery Use)
 */
class RequestBatteryOptimizationDisableUseCase(
    private val permissionRepository: PermissionRepository,
) {
    suspend operator fun invoke(): Result<PermissionStatus> {
        return permissionRepository.requestBatteryOptimizationDisable()
    }
}

