package com.shiplocate.data.datasource.impl

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.LocationRemoteDataSource
import com.shiplocate.data.model.LocationDataModel
import com.shiplocate.data.network.api.FlespiLocationApi
import com.shiplocate.data.network.api.OsmAndLocationApi

/**
 * Remote реализация LocationRemoteDataSource
 * Использует OsmAnd протокол для одиночных координат и Flespi протокол для пакетной отправки
 */
class LocationRemoteDataSourceImpl(
    private val osmAndLocationApi: OsmAndLocationApi,
    private val flespiLocationApi: FlespiLocationApi,
    private val logger: Logger,
) : LocationRemoteDataSource {
    override suspend fun sendLocation(
        serverLoadId: Long,
        location: LocationDataModel,
    ): Result<Unit> {
        logger.debug(LogCategory.NETWORK, "RemoteLocationDataSource: Sending single location to server")
        logger.debug(LogCategory.NETWORK, "RemoteLocationDataSource: Lat: ${location.latitude}, Lon: ${location.longitude}")
        return osmAndLocationApi.sendLocation(serverLoadId, location)
    }

    override suspend fun sendLocations(
        serverLoadId: Long,
        locations: List<LocationDataModel>,
    ): Result<Unit> {
        logger.debug(LogCategory.NETWORK, "RemoteLocationDataSource: Sending ${locations.size} locations to server")
        return try {
            if (locations.isEmpty()) {
                logger.debug(LogCategory.NETWORK, "RemoteLocationDataSource: No locations to send")
                return Result.success(Unit)
            }

            // Используем Flespi протокол для пакетной отправки
            logger.debug(LogCategory.NETWORK, "RemoteLocationDataSource: Using Flespi protocol for batch sending")
            val result = flespiLocationApi.sendLocations(serverLoadId, locations)

            if (result.isSuccess) {
                logger.debug(LogCategory.NETWORK, "RemoteLocationDataSource: ✅ Successfully sent ${locations.size} locations via Flespi protocol")
            } else {
                logger.debug(LogCategory.NETWORK, "RemoteLocationDataSource: ❌ Failed to send locations via Flespi protocol: ${result.exceptionOrNull()?.message}")
            }
            result
        } catch (e: Exception) {
            logger.debug(LogCategory.NETWORK, "RemoteLocationDataSource: ❌ Network error: ${e.message}")
            Result.failure(e)
        }
    }
}
