package com.tracker.domain.usecase

import com.tracker.domain.repository.LocationRepository

/**
 * Use Case для остановки GPS трекинга
 * Останавливает GPS трекинг
 */
class StopProcessLocationsUseCase(
    private val locationRepository: LocationRepository
) {
    
    suspend operator fun invoke(): Result<Unit> {
        return try {
            println("StopGpsTrackingUseCase: Stopping GPS tracking")
            val result = locationRepository.stopGpsTracking()
            if (result.isSuccess) {
                println("StopGpsTrackingUseCase: GPS tracking stopped successfully")
            } else {
                println("StopGpsTrackingUseCase: Failed to stop GPS tracking: ${result.exceptionOrNull()?.message}")
            }
            result
        } catch (e: Exception) {
            println("StopGpsTrackingUseCase: Error stopping GPS tracking: ${e.message}")
            Result.failure(e)
        }
    }
}

