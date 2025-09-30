package com.tracker.data.datasource.impl

import com.tracker.data.datasource.LocationRemoteDataSource
import com.tracker.data.model.LocationDataModel
import com.tracker.data.model.LocationRequestDataModel
import com.tracker.data.model.LocationResponseDataModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Remote реализация LocationRemoteDataSource
 */
class RemoteLocationDataSource(
    private val httpClient: HttpClient
) : LocationRemoteDataSource {
    
    // Базовый URL - в реальном приложении должен быть в конфигурации
    private val baseUrl = "https://your-api-server.com/api"
    
    override suspend fun sendLocations(locations: List<LocationDataModel>): Result<Unit> {
        return try {
            val request = LocationRequestDataModel(locations)
            val response = httpClient.post("$baseUrl/locations") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            val locationResponse: LocationResponseDataModel = response.body()
            
            if (locationResponse.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(locationResponse.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun checkConnection(): Result<Boolean> {
        return try {
            httpClient.get("$baseUrl/health")
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
