package com.shiplocate.domain.usecase

import com.shiplocate.domain.model.PermissionStatus
import com.shiplocate.domain.repository.PermissionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case для наблюдения за изменениями статуса разрешений
 * Возвращает Flow, который эмитит PermissionStatus при вызове notifyPermissionGranted()
 */
class ObservePermissionsUseCase(
    private val permissionRepository: PermissionRepository,
) {
    operator fun invoke(): Flow<PermissionStatus> {
        return permissionRepository.observePermissions()
    }
}

