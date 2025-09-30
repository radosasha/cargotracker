package com.tracker.domain.usecase

import com.tracker.domain.repository.LocationRepository

/**
 * Use Case для синхронизации GPS данных с сервером
 */
class SyncLocationsUseCase(
    private val locationRepository: LocationRepository
) {
    
    suspend operator fun invoke(): Result<Unit> {
        return locationRepository.syncLocationsToServer()
    }
}
