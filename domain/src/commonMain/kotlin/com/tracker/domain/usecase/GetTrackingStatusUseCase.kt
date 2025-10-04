package com.tracker.domain.usecase

import com.tracker.domain.model.TrackingStatus
import com.tracker.domain.repository.PrefsRepository

/**
 * Use Case для получения статуса трекинга
 * Просто читает состояние из DataStore
 */
class GetTrackingStatusUseCase(
    private val prefsRepository: PrefsRepository
) {
    
    suspend operator fun invoke(): TrackingStatus {
        // Получаем состояние только из DataStore
        val savedState = prefsRepository.getTrackingState()
        
        return if (savedState == true) {
            TrackingStatus.ACTIVE
        } else {
            TrackingStatus.STOPPED
        }
    }
}
