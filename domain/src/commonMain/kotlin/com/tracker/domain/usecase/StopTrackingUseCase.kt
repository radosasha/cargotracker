package com.tracker.domain.usecase

import com.tracker.domain.repository.TrackingRepository
import com.tracker.domain.service.LocationSyncService

/**
 * Use Case для остановки GPS трекинга
 */
class StopTrackingUseCase(
    private val trackingRepository: TrackingRepository,
    private val locationSyncManager: LocationSyncService
) {
    
    suspend operator fun invoke(): Result<Unit> {
        // Останавливаем синхронизацию
        locationSyncManager.stopSync()
        println("StopTrackingUseCase: Location sync stopped")
        
        // Останавливаем трекинг
        return trackingRepository.stopTracking()
    }
}
