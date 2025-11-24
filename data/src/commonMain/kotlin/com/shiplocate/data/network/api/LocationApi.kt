package com.shiplocate.data.network.api

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.model.LocationDataModel
import com.shiplocate.data.network.dto.CoordinateDto
import com.shiplocate.data.network.dto.CoordinatesRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

/**
 * API для отправки координат на сервер через мобильный API endpoint
 * Использует /api/mobile/loads/coordinates
 */
class LocationApi(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val logger: Logger,
) {
    /**
     * Отправляет координаты на сервер через мобильный API
     * @param token Bearer token для аутентификации
     * @param serverLoadId ID груза (server ID)
     * @param locations список координат для отправки
     * @return результат отправки
     */
    suspend fun sendCoordinates(
        token: String,
        serverLoadId: Long,
        locations: List<LocationDataModel>,
    ): Result<Unit> {
        return try {
            if (locations.isEmpty()) {
                logger.debug(LogCategory.NETWORK, "LocationApi: No locations to send")
                return Result.success(Unit)
            }

            // Преобразуем LocationDataModel в CoordinateDto
            // Важно: сервер ожидает lat/lon, bearing, batt, speed в knots
            val coordinates = locations.map { location ->
                // Конвертируем speed из m/s в knots (если есть)
                val speedInKnots = location.speed?.let { speedMps ->
                    // Конвертируем из m/s в knots: 1 m/s = 1.94384 knots
                    speedMps.toDouble() * 1.94384
                }
                
                CoordinateDto(
                    lat = location.latitude,
                    lon = location.longitude,
                    timestamp = location.timestamp.toEpochMilliseconds(),
                    valid = location.isValid,
                    speed = speedInKnots,
                    bearing = location.course?.toDouble(), // course -> bearing
                    heading = null, // используем bearing, heading не нужен
                    altitude = location.altitude,
                    accuracy = location.accuracy?.toDouble(),
                    batt = location.batteryLevel?.toDouble(), // batteryLevel -> batt (Double)
                    charge = null, // не передаем, если нет данных
                    hdop = null, // не передаем, если нет данных
                    driverUniqueId = null, // не передаем, если нет данных
                )
            }

            val request = CoordinatesRequestDto(
                loadId = serverLoadId,
                coordinates = coordinates,
            )

            logger.debug(LogCategory.NETWORK, "LocationApi: Sending ${locations.size} coordinates to $baseUrl/api/mobile/loads/coordinates")
            logger.debug(LogCategory.NETWORK, "LocationApi: Load ID: $serverLoadId")

            val response = httpClient.post {
                url("$baseUrl/api/mobile/loads/coordinates")
                contentType(ContentType.Application.Json)
                bearerAuth(token)
                setBody(request)
            }

            logger.debug(LogCategory.NETWORK, "LocationApi: Response status: ${response.status}")

            when (response.status) {
                HttpStatusCode.OK -> {
                    logger.debug(LogCategory.NETWORK, "LocationApi: ✅ Successfully sent ${locations.size} coordinates")
                    Result.success(Unit)
                }

                else -> {
                    val errorMessage = "HTTP ${response.status.value}: ${response.status.description}"
                    logger.debug(LogCategory.NETWORK, "LocationApi: ❌ Error: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            logger.debug(LogCategory.NETWORK, "LocationApi: ❌ Exception: ${e.message}")
            Result.failure(e)
        }
    }
}

