package com.tracker.domain.usecase

import com.tracker.domain.model.Location
import com.tracker.domain.repository.LocationRepository

/**
 * Use Case для получения последних GPS координат
 */
class GetRecentLocationsUseCase(
    private val locationRepository: LocationRepository
) {
    
    suspend operator fun invoke(limit: Int = 100): List<Location> {
        return locationRepository.getRecentLocations(limit)
    }
}
