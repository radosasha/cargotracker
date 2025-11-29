package com.shiplocate.domain.usecase

import com.shiplocate.domain.repository.PermissionRepository

class OpenAirplaneModeSettingsUseCase(
    private val permissionRepository: PermissionRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        return permissionRepository.openAirplaneModeSettings()
    }
}

