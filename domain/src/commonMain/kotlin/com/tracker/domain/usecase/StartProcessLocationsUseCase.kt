package com.tracker.domain.usecase

import com.tracker.domain.model.Location
import com.tracker.domain.repository.DeviceRepository
import com.tracker.domain.repository.LocationRepository
import com.tracker.domain.service.LocationProcessResult
import com.tracker.domain.service.LocationProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Use Case для обработки GPS координат
 * Запускает GPS трекинг и обрабатывает поток координат
 */
class StartProcessLocationsUseCase(
    private val locationRepository: LocationRepository,
    private val locationProcessor: LocationProcessor,
    private val deviceRepository: DeviceRepository
) {
    
    /**
     * Запускает GPS трекинг и начинает обработку координат
     */
    operator fun invoke(scope: CoroutineScope) {
        println("StartProcessLocationsUseCase: Starting GPS location processing")
        
        // Запускаем GPS трекинг и подписываемся на поток координат
        val locationFlow = locationRepository.startGpsTracking()
        
        locationFlow
            .onEach { location ->
                try {
                    println("StartProcessLocationsUseCase: 🔥 RECEIVED GPS location: Lat=${location.latitude}, Lon=${location.longitude}")
                    
                    // Обрабатываем координату
                    val result = processLocation(location)
                    
                    if (result.shouldSend) {
                        println("StartProcessLocationsUseCase: ✅ Successfully processed location")
                        println("StartProcessLocationsUseCase: Reason: ${result.reason}")
                    } else {
                        println("StartProcessLocationsUseCase: ⏭️ Location filtered out")
                        println("StartProcessLocationsUseCase: Reason: ${result.reason}")
                    }
                    
                } catch (e: Exception) {
                    println("StartProcessLocationsUseCase: ❌ Error processing location: ${e.message}")
                    e.printStackTrace()
                }
            }
            .catch { e ->
                println("StartProcessLocationsUseCase: Error in GPS flow: ${e.message}")
                e.printStackTrace()
            }
            .launchIn(scope)
    }
    
    /**
     * Обрабатывает одну GPS координату
     */
    private suspend fun processLocation(location: Location): LocationProcessResult {
        // Обрабатываем координату через LocationProcessor
        val processResult = locationProcessor.processLocation(location)
        
        // Если координата прошла фильтрацию, сохраняем в БД и пытаемся отправить
        if (processResult.shouldSend) {
            try {
                // Получаем уровень батареи
                val batteryLevel = deviceRepository.getBatteryLevel()
                
                // Сохраняем в БД
                val locationId = locationRepository.saveLocationToDb(location, batteryLevel)
                println("StartProcessLocationsUseCase: Location saved to DB with id: $locationId")
                
                // Получаем все неотправленные координаты из БД
                val unsentLocations = locationRepository.getUnsentLocations()
                println("StartProcessLocationsUseCase: Found ${unsentLocations.size} unsent locations in DB")
                
                // Определяем стратегию отправки
                val uploadResult = if (unsentLocations.size == 1) {
                    // Если только одна координата - отправляем через OsmAnd протокол
                    println("StartProcessLocationsUseCase: Sending single location via OsmAnd protocol")
                    locationRepository.sendLocation(location)
                } else {
                    // Если несколько координат - отправляем все через Flespi протокол
                    println("StartProcessLocationsUseCase: Sending ${unsentLocations.size} locations via Flespi protocol")
                    val locations = unsentLocations.map { it.second }
                    locationRepository.sendLocations(locations)
                }
                
                if (uploadResult.isSuccess) {
                    // Если отправка успешна - удаляем все отправленные координаты из БД
                    if (unsentLocations.size == 1) {
                        locationRepository.deleteLocationFromDb(locationId)
                        println("StartProcessLocationsUseCase: Single location uploaded and deleted from DB")
                    } else {
                        val ids = unsentLocations.map { it.first }
                        locationRepository.deleteLocationsFromDb(ids)
                        println("StartProcessLocationsUseCase: ${unsentLocations.size} locations uploaded and deleted from DB")
                    }
                    return processResult.copy(reason = "Successfully sent to server and deleted from DB")
                } else {
                    // Если отправка не удалась - оставляем в БД для последующей отправки
                    println("StartProcessLocationsUseCase: Locations saved to DB, will retry later: ${uploadResult.exceptionOrNull()?.message}")
                    return processResult.copy(reason = "Saved to DB, server upload failed (will retry later)")
                }
            } catch (e: Exception) {
                println("StartProcessLocationsUseCase: Error: ${e.message}")
                return processResult.copy(
                    shouldSend = false,
                    reason = "Failed to process location: ${e.message}"
                )
            }
        }
        
        return processResult
    }
}