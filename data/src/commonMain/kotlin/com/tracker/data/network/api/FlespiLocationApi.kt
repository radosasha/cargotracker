package com.tracker.data.network.api

import com.tracker.data.model.LocationDataModel
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * API для отправки координат на Traccar сервер через Flespi протокол
 * Поддерживает пакетную отправку координат
 */
class FlespiLocationApi(
    private val httpClient: HttpClient,
    private val serverUrl: String,
    private val deviceId: String
) {
    
    /**
     * Data класс для Flespi протокола
     */
    @Serializable
    data class FlespiPosition(
        val ident: String,
        val timestamp: Long,
        @SerialName("position.latitude") val latitude: Double,
        @SerialName("position.longitude") val longitude: Double,
        @SerialName("position.speed") val speed: Double = 0.0,
        @SerialName("position.direction") val direction: Double = 0.0,
        @SerialName("position.altitude") val altitude: Double = 0.0,
        @SerialName("position.satellites") val satellites: Int = 0,
        @SerialName("battery.level") val batteryLevel: Int = 0,
        @SerialName("position.valid") val valid: Boolean = true,
        @SerialName("position.accuracy") val accuracy: Double = 0.0
    )
    
    /**
     * Отправляет массив координат на сервер через Flespi протокол
     * @param locations список координат для отправки
     * @return результат отправки
     */
    suspend fun sendLocations(locations: List<LocationDataModel>): Result<Unit> {
        return try {
            if (locations.isEmpty()) {
                println("FlespiLocationApi: No locations to send")
                return Result.success(Unit)
            }
            
            val flespiPositions = locations.map { location ->
                FlespiPosition(
                    ident = deviceId,
                    timestamp = location.timestamp.toEpochMilliseconds() / 1000, // Flespi использует Unix timestamp в секундах
                    latitude = location.latitude,
                    longitude = location.longitude,
                    speed = location.speed?.toDouble() ?: 0.0,
                    direction = location.course?.toDouble() ?: 0.0,
                    altitude = location.altitude ?: 0.0,
                    satellites = 0, // Можно добавить если есть данные о спутниках
                    batteryLevel = location.batteryLevel ?: 0,
                    valid = location.isValid,
                    accuracy = location.accuracy?.toDouble() ?: 0.0
                )
            }
            
            val url = "$serverUrl:5149" // Flespi протокол использует порт 5149
            println("FlespiLocationApi: Sending ${locations.size} locations to $url")
            println("FlespiLocationApi: Device ID: $deviceId")
            
            val response = httpClient.post {
                url(url)
                contentType(ContentType.Application.Json)
                setBody(flespiPositions)
            }
            
            println("FlespiLocationApi: Response status: ${response.status}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("FlespiLocationApi: ✅ Successfully sent ${locations.size} locations")
                    Result.success(Unit)
                }
                else -> {
                    val errorMessage = "HTTP ${response.status.value}: ${response.status.description}"
                    println("FlespiLocationApi: ❌ Error: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            println("FlespiLocationApi: ❌ Exception: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Отправляет одну координату на сервер (для совместимости)
     * @param location координата для отправки
     * @return результат отправки
     */
    suspend fun sendLocation(location: LocationDataModel): Result<Unit> {
        return sendLocations(listOf(location))
    }
}
