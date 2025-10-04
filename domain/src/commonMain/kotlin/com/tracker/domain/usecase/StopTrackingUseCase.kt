package com.tracker.domain.usecase

import com.tracker.domain.repository.PrefsRepository
import com.tracker.domain.repository.TrackingRepository
import com.tracker.domain.service.LocationSyncService

/**
 * Use Case для остановки GPS трекинга
 * Сохраняет состояние в DataStore при успешной остановке
 */
class StopTrackingUseCase(
    private val trackingRepository: TrackingRepository,
    private val prefsRepository: PrefsRepository,
    private val locationSyncManager: LocationSyncService
) {
    
    suspend operator fun invoke(): Result<Unit> {
        // Проверяем, не остановлен ли уже трекинг
        val currentStatus = trackingRepository.getTrackingStatus()
        if (currentStatus == com.tracker.domain.model.TrackingStatus.STOPPED) {
            println("StopTrackingUseCase: Tracking is already stopped, no need to stop")
            // Убеждаемся, что состояние в DataStore корректное
            prefsRepository.saveTrackingState(false)
            return Result.success(Unit)
        }
        
        // Останавливаем синхронизацию
        locationSyncManager.stopSync()
        println("StopTrackingUseCase: Location sync stopped")
        
        // Останавливаем трекинг
        val result = trackingRepository.stopTracking()
        
        // Если трекинг успешно остановлен, сохраняем состояние в DataStore
        if (result.isSuccess) {
            prefsRepository.saveTrackingState(false)
            println("StopTrackingUseCase: Tracking stopped and state saved to DataStore")
        }
        
        return result
    }
}
