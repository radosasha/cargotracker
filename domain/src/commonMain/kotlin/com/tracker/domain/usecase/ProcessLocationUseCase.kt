package com.tracker.domain.usecase

import com.tracker.domain.model.Location
import com.tracker.domain.repository.LocationRepository
import com.tracker.domain.service.LocationProcessor
import com.tracker.domain.service.LocationProcessResult

/**
 * Use Case для обработки GPS координат
 * Содержит бизнес-логику фильтрации и отправки координат
 */
class ProcessLocationUseCase(
    private val locationRepository: LocationRepository,
    private val locationProcessor: LocationProcessor
) {
    
    suspend operator fun invoke(location: Location, batteryLevel: Float? = null): LocationProcessResult {
        // Обрабатываем координату через LocationProcessor
        val processResult = locationProcessor.processLocation(location)
        
        // Если координата прошла фильтрацию, сохраняем в БД и пытаемся отправить
        if (processResult.shouldSend) {
            try {
                // Сохраняем в БД
                val locationId = locationRepository.saveLocationToDb(location, batteryLevel)
                println("ProcessLocationUseCase: Location saved to DB with id: $locationId")
                
                // Получаем все неотправленные координаты из БД
                val unsentLocations = locationRepository.getUnsentLocations()
                println("ProcessLocationUseCase: Found ${unsentLocations.size} unsent locations in DB")
                
                // Определяем стратегию отправки
                val uploadResult = if (unsentLocations.size == 1) {
                    // Если только одна координата - отправляем через OsmAnd протокол
                    println("ProcessLocationUseCase: Sending single location via OsmAnd protocol")
                    locationRepository.sendLocation(location)
                } else {
                    // Если несколько координат - отправляем все через Flespi протокол
                    println("ProcessLocationUseCase: Sending ${unsentLocations.size} locations via Flespi protocol")
                    val locations = unsentLocations.map { it.second }
                    locationRepository.sendLocations(locations)
                }
                
                if (uploadResult.isSuccess) {
                    // Если отправка успешна - удаляем все отправленные координаты из БД
                    if (unsentLocations.size == 1) {
                        locationRepository.deleteLocationFromDb(locationId)
                        println("ProcessLocationUseCase: Single location uploaded and deleted from DB")
                    } else {
                        val ids = unsentLocations.map { it.first }
                        locationRepository.deleteLocationsFromDb(ids)
                        println("ProcessLocationUseCase: ${unsentLocations.size} locations uploaded and deleted from DB")
                    }
                    return processResult.copy(reason = "Successfully sent to server and deleted from DB")
                } else {
                    // Если отправка не удалась - оставляем в БД для последующей отправки
                    println("ProcessLocationUseCase: Locations saved to DB, will retry later: ${uploadResult.exceptionOrNull()?.message}")
                    return processResult.copy(reason = "Saved to DB, server upload failed (will retry later)")
                }
            } catch (e: Exception) {
                println("ProcessLocationUseCase: Error: ${e.message}")
                return processResult.copy(
                    shouldSend = false,
                    reason = "Failed to process location: ${e.message}"
                )
            }
        }
        
        return processResult
    }
}
