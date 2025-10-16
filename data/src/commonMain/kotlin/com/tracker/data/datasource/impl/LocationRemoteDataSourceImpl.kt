package com.tracker.data.datasource.impl

import com.tracker.data.datasource.LocationRemoteDataSource
import com.tracker.data.model.LocationDataModel
import com.tracker.data.network.api.FlespiLocationApi
import com.tracker.data.network.api.OsmAndLocationApi

/**
 * Remote реализация LocationRemoteDataSource
 * Использует OsmAnd протокол для одиночных координат и Flespi протокол для пакетной отправки
 */
class LocationRemoteDataSourceImpl(
    private val osmAndLocationApi: OsmAndLocationApi,
    private val flespiLocationApi: FlespiLocationApi,
) : LocationRemoteDataSource {
    override suspend fun sendLocation(
        loadId: String,
        location: LocationDataModel,
    ): Result<Unit> {
        println("RemoteLocationDataSource: Sending single location to server")
        println("RemoteLocationDataSource: Lat: ${location.latitude}, Lon: ${location.longitude}")
        return osmAndLocationApi.sendLocation(loadId, location)
    }

    override suspend fun sendLocations(
        loadId: String,
        locations: List<LocationDataModel>,
    ): Result<Unit> {
        println("RemoteLocationDataSource: Sending ${locations.size} locations to server")
        return try {
            if (locations.isEmpty()) {
                println("RemoteLocationDataSource: No locations to send")
                return Result.success(Unit)
            }

            // Используем Flespi протокол для пакетной отправки
            println("RemoteLocationDataSource: Using Flespi protocol for batch sending")
            val result = flespiLocationApi.sendLocations(loadId, locations)

            if (result.isSuccess) {
                println("RemoteLocationDataSource: ✅ Successfully sent ${locations.size} locations via Flespi protocol")
            } else {
                println("RemoteLocationDataSource: ❌ Failed to send locations via Flespi protocol: ${result.exceptionOrNull()?.message}")
            }
            result
        } catch (e: Exception) {
            println("RemoteLocationDataSource: ❌ Network error: ${e.message}")
            Result.failure(e)
        }
    }
}
