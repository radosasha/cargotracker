package com.shiplocate.domain.usecase

import com.shiplocate.domain.model.Location
import com.shiplocate.domain.repository.DeviceRepository
import com.shiplocate.domain.repository.LoadRepository
import com.shiplocate.domain.repository.LocationRepository
import com.shiplocate.domain.service.LocationProcessResult
import com.shiplocate.domain.service.LocationProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Use Case для обработки GPS координат
 * Запускает GPS трекинг и обрабатывает поток координат
 */
class StartProcessLocationsUseCase(
    private val locationRepository: LocationRepository,
    private val locationProcessor: LocationProcessor,
    private val deviceRepository: DeviceRepository,
    private val loadRepository: LoadRepository,
) {
    /**
     * Запускает GPS трекинг и возвращает Flow с результатами обработки координат
     * @return Flow<LocationProcessResult> поток результатов обработки GPS координат
     */
    suspend operator fun invoke(): Flow<LocationProcessResult> {
        println("StartProcessLocationsUseCase: Starting GPS location processing")

        // Запускаем GPS трекинг и конвертируем Flow<Location> в Flow<LocationProcessResult>
        val connectedLoad =
            withContext(Dispatchers.Default) {
                loadRepository.getConnectedLoad()
            } ?: throw IllegalStateException("Connected Load not found")
        return locationRepository.startGpsTracking(connectedLoad.loadId)
            .map { location ->
                try {
                    println("StartProcessLocationsUseCase: 🔥 RECEIVED GPS location: Lat=${location.latitude}, Lon=${location.longitude}")

                    // Обрабатываем координату
                    val result = processLocation(connectedLoad.loadId, location)

                    if (result.shouldSend) {
                        println("StartProcessLocationsUseCase: ✅ Successfully processed location")
                        println("StartProcessLocationsUseCase: Reason: ${result.reason}")
                    } else {
                        println("StartProcessLocationsUseCase: ⏭️ Location filtered out")
                        println("StartProcessLocationsUseCase: Reason: ${result.reason}")
                    }

                    result
                } catch (e: Exception) {
                    println("StartProcessLocationsUseCase: ❌ Error processing location: ${e.message}")
                    e.printStackTrace()

                    // Возвращаем ошибку как результат обработки
                    LocationProcessResult(
                        shouldSend = false,
                        reason = "Failed to process location: ${e.message}",
                        totalReceived = 0,
                        totalSent = 0,
                        lastSentTime = 0,
                        trackingStats = locationProcessor.createCurrentTrackingStats(),
                    )
                }
            }
            .catch { e ->
                println("StartProcessLocationsUseCase: Error in GPS flow: ${e.message}")
                e.printStackTrace()
                // Пропускаем ошибку в потоке, но логируем её
            }
    }

    /**
     * Обрабатывает одну GPS координату
     */
    private suspend fun processLocation(
        loadId: String,
        location: Location,
    ): LocationProcessResult {
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

                // Обновляем статистику сохранения
                locationProcessor.updateSavedLocation()

                // Получаем все неотправленные координаты из БД
                val unsentLocations = locationRepository.getUnsentLocations(loadId)
                println("StartProcessLocationsUseCase: Found ${unsentLocations.size} unsent locations in DB")

                // Определяем стратегию отправки
                val uploadResult =
                    if (unsentLocations.size == 1) {
                        // Если только одна координата - отправляем через OsmAnd протокол
                        println("StartProcessLocationsUseCase: Sending single location via OsmAnd protocol")
                        locationRepository.sendLocation(loadId, location)
                    } else {
                        // Если несколько координат - отправляем все через Flespi протокол
                        println("StartProcessLocationsUseCase: Sending ${unsentLocations.size} locations via Flespi protocol")
                        val locations = unsentLocations.map { it.second }
                        locationRepository.sendLocations(loadId, locations)
                    }

                if (uploadResult.isSuccess) {
                    // Обновляем статистику отправки
                    locationProcessor.updateSentLocations(location, unsentLocations.size)

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
                    // Если отправка не удалась - обновляем статистику ошибки
                    val errorMessage = uploadResult.exceptionOrNull()?.message ?: "Unknown error"
                    locationProcessor.updateSendError(location, errorMessage, "Server Upload Failed")

                    // Оставляем в БД для последующей отправки
                    println("StartProcessLocationsUseCase: Locations saved to DB, will retry later: $errorMessage")
                    return processResult.copy(reason = "Saved to DB, server upload failed (will retry later)")
                }
            } catch (e: Exception) {
                println("StartProcessLocationsUseCase: Error: ${e.message}")
                return processResult.copy(
                    shouldSend = false,
                    reason = "Failed to process location: ${e.message}",
                )
            }
        }

        return processResult
    }
}
