package com.tracker.domain.usecase

import com.tracker.domain.model.TrackingStatus
import com.tracker.domain.repository.TrackingRepository

/**
 * Use Case для получения статуса трекинга
 */
class GetTrackingStatusUseCase(
    private val trackingRepository: TrackingRepository
) {
    
    suspend operator fun invoke(): TrackingStatus {
        return trackingRepository.getTrackingStatus()
    }
}
