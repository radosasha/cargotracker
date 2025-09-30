package com.tracker.domain.usecase

import com.tracker.domain.repository.TrackingRepository

/**
 * Use Case для остановки GPS трекинга
 */
class StopTrackingUseCase(
    private val trackingRepository: TrackingRepository
) {
    
    suspend operator fun invoke(): Result<Unit> {
        return trackingRepository.stopTracking()
    }
}
