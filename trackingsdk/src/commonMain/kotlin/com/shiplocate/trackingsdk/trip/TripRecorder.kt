package com.shiplocate.trackingsdk.trip

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.GpsLocation
import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.model.load.Route
import com.shiplocate.domain.repository.AuthRepository
import com.shiplocate.domain.repository.DeviceRepository
import com.shiplocate.domain.repository.GpsRepository
import com.shiplocate.domain.repository.LoadRepository
import com.shiplocate.domain.repository.LocationRepository
import com.shiplocate.domain.repository.RouteRepository
import com.shiplocate.domain.service.LocationProcessResult
import com.shiplocate.domain.service.LocationProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

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
    private val routeRepository: RouteRepository,
    private val authPrefsRepository: AuthRepository,
    private val logger: Logger,
) {

    private var route: Route? = null

    /**
     * Запускает GPS трекинг и возвращает Flow с результатами обработки координат
     * @return Flow<LocationProcessResult> поток результатов обработки GPS координат
     */
    suspend fun startTracking(loadId: Long): Flow<LocationProcessResult> {
        logger.info(LogCategory.LOCATION, "TripRecorder: Starting GPS location processing")

        // Запускаем GPS трекинг и конвертируем Flow<Location> в Flow<LocationProcessResult>
        val connectedLoad = withContext(Dispatchers.Default) {
            loadRepository.getLoadById(loadId)
        } ?: throw IllegalStateException("Connected Load not found")

        route = routeRepository.getCachedRoute(loadId)

        return gpsRepository.startGpsTracking()
            .map { location ->
                logger.info(
                    LogCategory.LOCATION,
                    "TripRecorder: 🔥 RECEIVED GPS location: Lat=${location.latitude}, Lon=${location.longitude}"
                )

                // FIXME mock location
                val location = GpsLocation(latitude = 45.49760, longitude = -73.749417, accuracy = 10.0f, timestamp = Clock.System.now())
                if (routeRepository.getRequireUpdate()) {
                    logger.debug(LogCategory.LOCATION, "TripRecorder: First GPS location received, checking route update requirement")
                    val authSession = authPrefsRepository.getSession()
                    val token = authSession?.token
                    if (token == null) {
                        logger.warn(LogCategory.LOCATION, "TripRecorder: Cannot request route update - no auth session")
                    } else {
                        val routeResult = fetchRoute(token, connectedLoad, location.latitude, location.longitude)
                        routeResult.fold({
                            route = it
                        }, {
                            logger.info(LogCategory.LOCATION, "unable to fetch route: ${it.message}")
                        })
                    }
                }

                // Обрабатываем координату
                // Используем loadName только для отправки на сервер (OsmAnd протокол ожидает uniqueId)
                val result = processLocation(connectedLoad.serverId, location)

                if (result.shouldSend) {
                    logger.info(
                        LogCategory.LOCATION,
                        "TripRecorder: ✅ Successfully processed location\nTripRecorder: Reason: ${result.reason}"
                    )
                } else {
                    logger.info(LogCategory.LOCATION, "TripRecorder: ⏭️ Location filtered out\nTripRecorder: Reason: ${result.reason}")
                }
                result
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
        serverId: Long,
        location: GpsLocation,
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
                val unsentLocations = locationRepository.getUnsentDeviceLocations()
                logger.debug(LogCategory.LOCATION, "TripRecorder: Found ${unsentLocations.size} unsent locations in DB")

                // Получаем токен для аутентификации
                val authSession = authPrefsRepository.getSession()
                if (authSession == null) {
                    logger.error(LogCategory.LOCATION, "TripRecorder: No auth session found, cannot send coordinates")
                    return processResult.copy(
                        shouldSend = false,
                        reason = "No auth session found",
                        lastCoordinateLat = location.latitude,
                        lastCoordinateLon = location.longitude,
                        lastCoordinateTime = location.timestamp.toEpochMilliseconds(),
                        coordinateErrorMeters = location.accuracy.toInt(),
                    )
                }

                // Отправляем все координаты через мобильный API
                logger.debug(LogCategory.LOCATION, "TripRecorder: Sending ${unsentLocations.size} locations via mobile API")
                val locations = unsentLocations.map { it.second }
                // TODO send chunks
                val uploadResult = locationRepository.sendLocations(authSession.token, serverId, locations)

                if (uploadResult.isSuccess) {
                    // Обновляем статистику отправки
                    locationProcessor.updateSentLocations(location, unsentLocations.size)

                    // Если отправка успешна - удаляем все отправленные координаты из БД
                    val ids = unsentLocations.map { it.first }
                    locationRepository.deleteLocationsFromDb(ids)
                    logger.debug(LogCategory.LOCATION, "TripRecorder: ${unsentLocations.size} locations uploaded and deleted from DB")
                    return processResult.copy(
                        reason = "Successfully sent to server and deleted from DB",
                        lastCoordinateLat = location.latitude,
                        lastCoordinateLon = location.longitude,
                        lastCoordinateTime = location.timestamp.toEpochMilliseconds(),
                        coordinateErrorMeters = location.accuracy.toInt(),
                    )
                } else {
                    // Если отправка не удалась - обновляем статистику ошибки
                    val errorMessage = uploadResult.exceptionOrNull()?.message ?: "Unknown error"
                    locationProcessor.updateSendError(location, errorMessage, "Server Upload Failed")

                    // Оставляем в БД для последующей отправки
                    logger.debug(LogCategory.LOCATION, "TripRecorder: Locations saved to DB, will retry later: $errorMessage")
                    return processResult.copy(
                        reason = "Saved to DB, server upload failed (will retry later)",
                        lastCoordinateLat = location.latitude,
                        lastCoordinateLon = location.longitude,
                        lastCoordinateTime = location.timestamp.toEpochMilliseconds(),
                        coordinateErrorMeters = location.accuracy.toInt(),
                    )
                }
            } catch (e: Exception) {
                logger.error(LogCategory.LOCATION, "TripRecorder: Error: ${e.message}", e)
                return processResult.copy(
                    shouldSend = false,
                    reason = "Failed to process location: ${e.message}",
                    lastCoordinateLat = location.latitude,
                    lastCoordinateLon = location.longitude,
                    lastCoordinateTime = location.timestamp.toEpochMilliseconds(),
                    coordinateErrorMeters = location.accuracy.toInt(),
                )
            }
        }

        return processResult.copy(
            lastCoordinateLat = location.latitude,
            lastCoordinateLon = location.longitude,
            lastCoordinateTime = location.timestamp.toEpochMilliseconds(),
            coordinateErrorMeters = location.accuracy.toInt(),
        )
    }

    private suspend fun fetchRoute(token: String, load: Load, startLat: Double, startLon: Double): Result<Route> {
        logger.info(
            LogCategory.LOCATION,
            "TripRecorder: Requesting route update for load ${load.serverId} after first GPS location with start coordinates ($startLat, $startLon)"
        )
        val routeResult = routeRepository.getRoute(token, load.serverId, startLat, startLon)
        return routeResult.fold(
            onSuccess = { route ->
                routeRepository.saveRoute(
                    loadId = load.id,
                    route = route,
                    provider = "google",
                    requireUpdate = false,
                )
                logger.info(LogCategory.LOCATION, "TripRecorder: Route updated successfully for load ${load.serverId}")
                Result.success(route)
            },
            onFailure = { error ->
                logger.error(
                    LogCategory.LOCATION,
                    "TripRecorder: Failed to update route for load ${load.serverId}: ${error.message}",
                    error
                )
                Result.failure(error)
            },
        )
    }
}
