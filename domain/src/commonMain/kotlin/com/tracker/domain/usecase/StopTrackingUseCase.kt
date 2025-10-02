package com.tracker.domain.usecase

import com.tracker.domain.repository.TrackingRepository
import com.tracker.domain.service.LocationSyncService

/**
 * Use Case для остановки GPS трекинга
 */
class StopTrackingUseCase(
    private val trackingRepository: TrackingRepository,
    private val locationSyncService: LocationSyncService
) {
    
    suspend operator fun invoke(): Result<Unit> {
        // Останавливаем синхронизацию
        locationSyncService.stopSync()
        println("StopTrackingUseCase: Location sync service stopped")
        
        // Останавливаем трекинг
        return trackingRepository.stopTracking()
    }
}
