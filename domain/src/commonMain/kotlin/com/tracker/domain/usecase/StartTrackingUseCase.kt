package com.tracker.domain.usecase

import com.tracker.domain.repository.PermissionRepository
import com.tracker.domain.repository.PrefsRepository
import com.tracker.domain.repository.TrackingRepository
import com.tracker.domain.service.LocationSyncService

/**
 * Use Case для запуска GPS трекинга
 * Сохраняет состояние в DataStore при успешном запуске
 */
class StartTrackingUseCase(
    private val permissionRepository: PermissionRepository,
    private val trackingRepository: TrackingRepository,
    private val prefsRepository: PrefsRepository,
    private val locationSyncManager: LocationSyncService
) {
    
    suspend operator fun invoke(): Result<Unit> {
        // Проверяем разрешения перед запуском
        val permissionStatus = permissionRepository.getPermissionStatus()
        
        return if (permissionStatus.hasAllPermissions) {
            // Проверяем, не активен ли уже трекинг
            val currentStatus = trackingRepository.getTrackingStatus()
            if (currentStatus == com.tracker.domain.model.TrackingStatus.ACTIVE) {
                println("StartTrackingUseCase: Tracking is already active, no need to start")
                // Убеждаемся, что состояние в DataStore корректное
                prefsRepository.saveTrackingState(true)
                return Result.success(Unit)
            }
            
            val result = trackingRepository.startTracking()
            
            // Если трекинг успешно запущен, сохраняем состояние в DataStore
            if (result.isSuccess) {
                prefsRepository.saveTrackingState(true)
                locationSyncManager.startSync()
                println("StartTrackingUseCase: Tracking started and state saved to DataStore")
            }
            
            result
        } else {
            Result.failure(
                IllegalStateException("Не все необходимые разрешения получены")
            )
        }
    }
}
