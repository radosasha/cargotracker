package com.shiplocate.data.datasource.impl

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.LocationRemoteDataSource
import com.shiplocate.data.model.LocationDataModel
import com.shiplocate.data.network.api.LocationApi

/**
 * Remote реализация LocationRemoteDataSource
 * Использует мобильный API для отправки координат
 */
class LocationRemoteDataSourceImpl(
    private val locationApi: LocationApi,
    private val logger: Logger,
) : LocationRemoteDataSource {
    override suspend fun sendLocations(
        token: String,
        serverLoadId: Long,
        locations: List<LocationDataModel>,
    ): Result<Unit> {
        logger.debug(LogCategory.NETWORK, "RemoteLocationDataSource: Sending ${locations.size} locations to server")
        return try {
            if (locations.isEmpty()) {
                logger.debug(LogCategory.NETWORK, "RemoteLocationDataSource: No locations to send")
                return Result.success(Unit)
            }

            logger.debug(LogCategory.NETWORK, "RemoteLocationDataSource: Using mobile API for batch sending")
            val result = locationApi.sendCoordinates(token, serverLoadId, locations)

            if (result.isSuccess) {
                logger.debug(LogCategory.NETWORK, "RemoteLocationDataSource: ✅ Successfully sent ${locations.size} locations via mobile API")
            } else {
                logger.debug(LogCategory.NETWORK, "RemoteLocationDataSource: ❌ Failed to send locations via mobile API: ${result.exceptionOrNull()?.message}")
            }
            result
        } catch (e: Exception) {
            logger.error(LogCategory.NETWORK, "RemoteLocationDataSource: ❌ Network error: ${e.message}")
            Result.failure(e)
        }
    }
}
