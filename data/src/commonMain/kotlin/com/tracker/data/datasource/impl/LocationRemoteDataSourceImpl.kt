package com.tracker.data.datasource.impl

import com.tracker.data.datasource.LocationRemoteDataSource
import com.tracker.data.model.LocationDataModel
import com.tracker.data.network.api.LocationApi

/**
 * Remote реализация LocationRemoteDataSource
 */
class LocationRemoteDataSourceImpl(
    private val locationApi: LocationApi
) : LocationRemoteDataSource {
    
    override suspend fun sendLocation(location: LocationDataModel): Result<Unit> {
        println("RemoteLocationDataSource: Sending single location to server")
        println("RemoteLocationDataSource: Lat: ${location.latitude}, Lon: ${location.longitude}")
        return locationApi.sendLocation(location)
    }
    
    override suspend fun sendLocations(locations: List<LocationDataModel>): Result<Unit> {
        println("RemoteLocationDataSource: Sending ${locations.size} locations to server")
        return try {
            // Отправляем каждую координату отдельно через LocationApi
            for (location in locations) {
                val result = locationApi.sendLocation(location)
                if (result.isFailure) {
                    return result
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            println("RemoteLocationDataSource: ❌ Network error: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun checkConnection(): Result<Boolean> {
        // Для проверки соединения можно попробовать отправить тестовую координату
        return try {
            val testLocation = LocationDataModel(
                latitude = 0.0,
                longitude = 0.0,
                timestamp = kotlinx.datetime.Clock.System.now(),
                isValid = false
            )
            locationApi.sendLocation(testLocation)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
