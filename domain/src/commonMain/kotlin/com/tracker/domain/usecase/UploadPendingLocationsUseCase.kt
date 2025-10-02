package com.tracker.domain.usecase

import com.tracker.domain.repository.LocationRepository

/**
 * Use Case для отправки всех неотправленных координат на сервер
 * Используется при восстановлении связи
 */
class UploadPendingLocationsUseCase(
    private val locationRepository: LocationRepository
) {
    
    /**
     * Получает все неотправленные координаты и отправляет их пакетом
     * Удаляет успешно отправленные
     */
    suspend operator fun invoke(): Result<Int> {
        return try {
            val unsentLocations = locationRepository.getUnsentLocations()
            
            if (unsentLocations.isEmpty()) {
                println("UploadPendingLocationsUseCase: No pending locations to upload")
                return Result.success(0)
            }
            
            println("UploadPendingLocationsUseCase: Found ${unsentLocations.size} pending locations")
            
            var successCount = 0
            val successfulIds = mutableListOf<Long>()
            
            // Отправляем каждую координату
            unsentLocations.forEach { (id, location) ->
                val result = locationRepository.saveLocation(location)
                if (result.isSuccess) {
                    successCount++
                    successfulIds.add(id)
                    println("UploadPendingLocationsUseCase: Location $id uploaded successfully")
                } else {
                    println("UploadPendingLocationsUseCase: Failed to upload location $id: ${result.exceptionOrNull()?.message}")
                }
            }
            
            // Удаляем успешно отправленные
            if (successfulIds.isNotEmpty()) {
                locationRepository.deleteLocationsFromDb(successfulIds)
                println("UploadPendingLocationsUseCase: Deleted $successCount locations from DB")
            }
            
            Result.success(successCount)
        } catch (e: Exception) {
            println("UploadPendingLocationsUseCase: Error: ${e.message}")
            Result.failure(e)
        }
    }
}

