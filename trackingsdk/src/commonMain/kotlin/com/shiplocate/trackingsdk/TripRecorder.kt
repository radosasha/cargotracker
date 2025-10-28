package com.shiplocate.trackingsdk

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.Location
import com.shiplocate.domain.repository.DeviceRepository
import com.shiplocate.domain.repository.GpsRepository
import com.shiplocate.domain.repository.LoadRepository
import com.shiplocate.domain.repository.LocationRepository
import com.shiplocate.domain.repository.PermissionRepository
import com.shiplocate.domain.repository.PrefsRepository
import com.shiplocate.domain.repository.TrackingRepository
import com.shiplocate.domain.service.LocationProcessResult
import com.shiplocate.domain.service.LocationProcessor
import com.shiplocate.domain.service.LocationSyncService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * TripRecorder - объединяет логику StartTrackerUseCase и StopTrackerUseCase
 * Управляет GPS трекингом и обработкой координат
 */
class TripRecorder(
    private val locationRepository: LocationRepository,
    private val gpsRepository: GpsRepository,
    private val locationProcessor: LocationProcessor,
    private val deviceRepository: DeviceRepository,
    private val loadRepository: LoadRepository,
    private val permissionRepository: PermissionRepository,
    private val trackingRepository: TrackingRepository,
    private val prefsRepository: PrefsRepository,
    private val locationSyncService: LocationSyncService,
    private val logger: Logger,
) {

    /**
     * Запускает GPS трекинг и возвращает Flow с результатами обработки координат
     * @return Flow<LocationProcessResult> поток результатов обработки GPS координат
     */
    suspend fun startTracking(): Flow<LocationProcessResult> {
        logger.info(LogCategory.LOCATION, "TripRecorder: Starting GPS location processing")

        // Запускаем GPS трекинг и конвертируем Flow<Location> в Flow<LocationProcessResult>
        val connectedLoad =
            withContext(Dispatchers.Default) {
                loadRepository.getConnectedLoad()
            } ?: throw IllegalStateException("Connected Load not found")
        
        return gpsRepository.startGpsTracking()
            .map { location ->
                try {
                    logger.debug(LogCategory.LOCATION, "TripRecorder: 🔥 RECEIVED GPS location: Lat=${location.latitude}, Lon=${location.longitude}")

                    // Обрабатываем координату
                    val result = processLocation(connectedLoad.loadId, location)

                    if (result.shouldSend) {
                        logger.debug(LogCategory.LOCATION, "TripRecorder: ✅ Successfully processed location")
                        logger.debug(LogCategory.LOCATION, "TripRecorder: Reason: ${result.reason}")
                    } else {
                        logger.debug(LogCategory.LOCATION, "TripRecorder: ⏭️ Location filtered out")
                        logger.debug(LogCategory.LOCATION, "TripRecorder: Reason: ${result.reason}")
                    }

                    result
                } catch (e: Exception) {
                    logger.error(LogCategory.LOCATION, "TripRecorder: ❌ Error processing location: ${e.message}", e)

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
                logger.error(LogCategory.LOCATION, "TripRecorder: Error in GPS flow: ${e.message}", e)
                // Пропускаем ошибку в потоке, но логируем её
            }
    }

    /**
     * Останавливает GPS трекинг
     */
    suspend fun stopTracking(): Result<Unit> {
        return try {
            logger.info(LogCategory.LOCATION, "TripRecorder: Stopping GPS tracking")
            val result = gpsRepository.stopGpsTracking()
            if (result.isSuccess) {
                logger.info(LogCategory.LOCATION, "TripRecorder: GPS tracking stopped successfully")
            } else {
                logger.error(LogCategory.LOCATION, "TripRecorder: Failed to stop GPS tracking: ${result.exceptionOrNull()?.message}")
            }
            result
        } catch (e: Exception) {
            logger.error(LogCategory.LOCATION, "TripRecorder: Error stopping GPS tracking: ${e.message}", e)
            Result.failure(e)
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
                logger.debug(LogCategory.LOCATION, "TripRecorder: Location saved to DB with id: $locationId")

                // Обновляем статистику сохранения
                locationProcessor.updateSavedLocation()

                // Получаем все неотправленные координаты из БД
                val unsentLocations = locationRepository.getUnsentLocations(loadId)
                logger.debug(LogCategory.LOCATION, "TripRecorder: Found ${unsentLocations.size} unsent locations in DB")

                // Определяем стратегию отправки
                val uploadResult =
                    if (unsentLocations.size == 1) {
                        // Если только одна координата - отправляем через OsmAnd протокол
                        logger.debug(LogCategory.LOCATION, "TripRecorder: Sending single location via OsmAnd protocol")
                        locationRepository.sendLocation(loadId, location)
                    } else {
                        // Если несколько координат - отправляем все через Flespi протокол
                        logger.debug(LogCategory.LOCATION, "TripRecorder: Sending ${unsentLocations.size} locations via Flespi protocol")
                        val locations = unsentLocations.map { it.second }
                        locationRepository.sendLocations(loadId, locations)
                    }

                if (uploadResult.isSuccess) {
                    // Обновляем статистику отправки
                    locationProcessor.updateSentLocations(location, unsentLocations.size)

                    // Если отправка успешна - удаляем все отправленные координаты из БД
                    if (unsentLocations.size == 1) {
                        locationRepository.deleteLocationFromDb(locationId)
                        logger.debug(LogCategory.LOCATION, "TripRecorder: Single location uploaded and deleted from DB")
                    } else {
                        val ids = unsentLocations.map { it.first }
                        locationRepository.deleteLocationsFromDb(ids)
                        logger.debug(LogCategory.LOCATION, "TripRecorder: ${unsentLocations.size} locations uploaded and deleted from DB")
                    }
                    return processResult.copy(reason = "Successfully sent to server and deleted from DB")
                } else {
                    // Если отправка не удалась - обновляем статистику ошибки
                    val errorMessage = uploadResult.exceptionOrNull()?.message ?: "Unknown error"
                    locationProcessor.updateSendError(location, errorMessage, "Server Upload Failed")

                    // Оставляем в БД для последующей отправки
                    logger.debug(LogCategory.LOCATION, "TripRecorder: Locations saved to DB, will retry later: $errorMessage")
                    return processResult.copy(reason = "Saved to DB, server upload failed (will retry later)")
                }
            } catch (e: Exception) {
                logger.error(LogCategory.LOCATION, "TripRecorder: Error: ${e.message}", e)
                return processResult.copy(
                    shouldSend = false,
                    reason = "Failed to process location: ${e.message}",
                )
            }
        }

        return processResult
    }
}
