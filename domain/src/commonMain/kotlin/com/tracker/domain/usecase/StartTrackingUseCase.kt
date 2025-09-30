package com.tracker.domain.usecase

import com.tracker.domain.repository.PermissionRepository
import com.tracker.domain.repository.TrackingRepository

/**
 * Use Case для запуска GPS трекинга
 */
class StartTrackingUseCase(
    private val permissionRepository: PermissionRepository,
    private val trackingRepository: TrackingRepository
) {
    
    suspend operator fun invoke(): Result<Unit> {
        // Проверяем разрешения перед запуском
        val permissionStatus = permissionRepository.getPermissionStatus()
        
        return if (permissionStatus.hasAllPermissions) {
            trackingRepository.startTracking()
        } else {
            Result.failure(
                IllegalStateException("Не все необходимые разрешения получены")
            )
        }
    }
}
