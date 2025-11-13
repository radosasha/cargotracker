package com.shiplocate.domain.usecase

import com.shiplocate.domain.repository.PermissionRepository

/**
 * Use case для уведомления о том, что разрешения были получены
 * Вызывает PermissionRepository.notifyPermissionGranted(), который эмитит событие в Flow
 */
class NotifyPermissionGrantedUseCase(
    private val permissionRepository: PermissionRepository,
) {
    suspend operator fun invoke() {
        permissionRepository.notifyPermissionGranted()
    }
}

