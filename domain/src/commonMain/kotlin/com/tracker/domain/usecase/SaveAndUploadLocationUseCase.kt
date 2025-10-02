package com.tracker.domain.usecase

import com.tracker.domain.model.Location
import com.tracker.domain.repository.LocationRepository

/**
 * Use Case для сохранения координаты в БД и отправки на сервер
 */
class SaveAndUploadLocationUseCase(
    private val locationRepository: LocationRepository
) {
    
    /**
     * Сохраняет координату в БД и пытается отправить на сервер
     * Если отправка успешна - удаляет из БД
     */
    suspend operator fun invoke(location: Location, batteryLevel: Float? = null): Result<Unit> {
        return try {
            // Сохраняем в БД
            val locationId = locationRepository.saveLocationToDb(location, batteryLevel)
            println("SaveAndUploadLocationUseCase: Location saved to DB with id: $locationId")
            
            // Пытаемся отправить на сервер
            val uploadResult = locationRepository.saveLocation(location)
            
            if (uploadResult.isSuccess) {
                // Если отправка успешна - удаляем из БД
                locationRepository.deleteLocationFromDb(locationId)
                println("SaveAndUploadLocationUseCase: Location uploaded and deleted from DB")
                Result.success(Unit)
            } else {
                // Если отправка не удалась - оставляем в БД для последующей отправки
                println("SaveAndUploadLocationUseCase: Location saved to DB, will retry later: ${uploadResult.exceptionOrNull()?.message}")
                Result.success(Unit) // Все равно success, т.к. сохранили в БД
            }
        } catch (e: Exception) {
            println("SaveAndUploadLocationUseCase: Error: ${e.message}")
            Result.failure(e)
        }
    }
}

