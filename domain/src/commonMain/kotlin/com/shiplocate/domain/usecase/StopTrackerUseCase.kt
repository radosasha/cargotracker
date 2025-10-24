package com.shiplocate.domain.usecase

import com.shiplocate.domain.repository.GpsRepository

/**
 * Use Case для остановки GPS трекинга
 * Останавливает GPS трекинг
 */
class StopTrackerUseCase(
    private val gpsRepository: GpsRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            println("StopGpsTrackingUseCase: Stopping GPS tracking")
            val result = gpsRepository.stopGpsTracking()
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
