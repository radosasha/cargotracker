package com.tracker.data.network.api

import com.tracker.data.model.LocationDataModel
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.contentType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * API для отправки координат на Traccar сервер через OsmAnd протокол
 */
class OsmAndLocationApi(
    private val httpClient: HttpClient,
    private val serverUrl: String,
    private val deviceId: String
) {
    
    /**
     * Отправляет координаты на сервер через OsmAnd протокол
     * @param location данные о местоположении
     * @return результат отправки
     */
    suspend fun sendLocation(location: LocationDataModel): Result<Unit> {
        return try {
            val url = "$serverUrl/api/osmand"
            println("LocationApi.sendLocation() - URL: $url")
            println("LocationApi.sendLocation() - Device ID: $deviceId")
            println("LocationApi.sendLocation() - Location: lat=${location.latitude}, lon=${location.longitude}")
            println("LocationApi.sendLocation() - Timestamp: ${location.timestamp}")
            
            val response = httpClient.submitForm(
                url = url,
                formParameters = Parameters.build {
                    append("id", deviceId)
                    append("lat", location.latitude.toString())
                    append("lon", location.longitude.toString())
                    append("timestamp", location.timestamp.toEpochMilliseconds().toString())
                    append("valid", location.isValid.toString())
                    
                    // Дополнительные параметры если доступны
                    location.speed?.let { append("speed", it.toString()) }
                    location.course?.let { append("bearing", it.toString()) }
                    location.altitude?.let { append("altitude", it.toString()) }
                    location.accuracy?.let { append("accuracy", it.toString()) }
                    location.batteryLevel?.let { append("batt", it.toString()) }
                }
            )
            
            println("LocationApi.sendLocation() - Response status: ${response.status}")
            println("LocationApi.sendLocation() - Response headers: ${response.headers}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("LocationApi.sendLocation() - success: ${response.status}")
                    Result.success(Unit)
                }
                else -> {
                    val errorMessage = "HTTP ${response.status.value}: ${response.status.description}"
                    println("LocationApi.sendLocation() - error: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            println("LocationApi.sendLocation() - exception: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Отправляет координаты в JSON формате (альтернативный способ)
     */
    suspend fun sendLocationJson(location: LocationDataModel): Result<Unit> {
        return try {
            println("LocationApi.sendLocationJson() - sending JSON location")
            
            val jsonData = buildJsonObject {
                put("device_id", deviceId)
                putJsonObject("location") {
                    put("timestamp", location.timestamp.toString())
                    putJsonObject("coords") {
                        put("latitude", location.latitude)
                        put("longitude", location.longitude)
                        location.speed?.let { put("speed", it) }
                        location.course?.let { put("heading", it) }
                        location.altitude?.let { put("altitude", it) }
                        location.accuracy?.let { put("accuracy", it) }
                    }
                    location.batteryLevel?.let { battery ->
                        putJsonObject("battery") {
                            put("level", battery / 100.0)
                            put("is_charging", false) // TODO: получить реальное состояние зарядки
                        }
                    }
                    put("is_moving", location.speed?.let { it > 0 } ?: false)
                }
            }
            
            val response = httpClient.post {
                url("$serverUrl/api/osmand")
                contentType(ContentType.Application.Json)
                setBody(jsonData)
            }
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("LocationApi.sendLocationJson() - success: ${response.status}")
                    Result.success(Unit)
                }
                else -> {
                    val errorMessage = "HTTP ${response.status.value}: ${response.status.description}"
                    println("LocationApi.sendLocationJson() - error: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            println("LocationApi.sendLocationJson() - exception: ${e.message}")
            Result.failure(e)
        }
    }
}
