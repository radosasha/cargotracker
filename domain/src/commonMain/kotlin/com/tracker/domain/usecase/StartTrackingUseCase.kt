package com.tracker.domain.usecase

import com.tracker.domain.repository.PermissionRepository
import com.tracker.domain.repository.TrackingRepository
import com.tracker.domain.service.LocationSyncService

/**
 * Use Case для запуска GPS трекинга
 */
class StartTrackingUseCase(
    private val permissionRepository: PermissionRepository,
    private val trackingRepository: TrackingRepository,
    private val locationSyncManager: LocationSyncService
) {
    
    suspend operator fun invoke(): Result<Unit> {
        // Проверяем разрешения перед запуском
        val permissionStatus = permissionRepository.getPermissionStatus()
        
        return if (permissionStatus.hasAllPermissions) {
            val result = trackingRepository.startTracking()
            
            // Запускаем синхронизацию неотправленных координат
            if (result.isSuccess) {
                locationSyncManager.startSync()
                println("StartTrackingUseCase: Location sync started")
            }
            
            result
        } else {
            Result.failure(
                IllegalStateException("Не все необходимые разрешения получены")
            )
        }
    }
}
