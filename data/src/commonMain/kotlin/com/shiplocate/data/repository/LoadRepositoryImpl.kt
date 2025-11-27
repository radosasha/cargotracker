package com.shiplocate.data.repository

import com.shiplocate.core.database.entity.LoadEntity
import com.shiplocate.core.database.entity.StopEntity
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.load.LoadsLocalDataSource
import com.shiplocate.data.datasource.load.LoadsRemoteDataSource
import com.shiplocate.data.datasource.load.StopsLocalDataSource
import com.shiplocate.data.mapper.toDomain
import com.shiplocate.data.mapper.toEntity
import com.shiplocate.data.mapper.toStopEntity
import com.shiplocate.data.network.dto.load.LoadDto
import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.model.load.LoadStatus
import com.shiplocate.domain.model.load.Stop
import com.shiplocate.domain.repository.LoadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of LoadRepository
 * Handles fetching loads from server with automatic caching fallback
 */
class LoadRepositoryImpl(
    private val loadsRemoteDataSource: LoadsRemoteDataSource,
    private val loadsLocalDataSource: LoadsLocalDataSource,
    private val stopsLocalDataSource: StopsLocalDataSource,
    private val logger: Logger,
) : LoadRepository {
    override suspend fun getLoads(token: String): Result<List<Load>> {
        logger.info(LogCategory.GENERAL, "🔄 LoadRepositoryImpl: Getting loads with token")

        return try {
            // Try to fetch from server
            logger.info(LogCategory.GENERAL, "🌐 LoadRepositoryImpl: Fetching from server")
            val loadDtos = loadsRemoteDataSource.getLoads(token)

            // Cache the results
            logger.info(LogCategory.GENERAL, "💾 LoadRepositoryImpl: Remove previous cached loads")

            saveLoads(loadDtos)

            // Return domain models
            val loads = getCachedLoads()
            logger.info(LogCategory.GENERAL, "✅ LoadRepositoryImpl: Successfully loaded ${loads.size} loads from server")
            Result.success(loads)
        } catch (e: Exception) {
            // Server failed, try cache
            logger.info(LogCategory.GENERAL, "⚠️ LoadRepositoryImpl: Server request failed, falling back to cache: ${e.message}")

            try {
                val loads = getCachedLoads()
                if (loads.isNotEmpty()) {
                    logger.info(LogCategory.GENERAL, "✅ LoadRepositoryImpl: Loaded ${loads.size} loads from cache")
                    Result.success(loads)
                } else {
                    logger.info(LogCategory.GENERAL, "❌ LoadRepositoryImpl: No cached loads available")
                    Result.failure(Exception("No cached data available. Please check your connection."))
                }
            } catch (cacheError: Exception) {
                logger.info(LogCategory.GENERAL, "❌ LoadRepositoryImpl: Cache read failed: ${cacheError.message}")
                Result.failure(Exception("Failed to load data: ${e.message}"))
            }
        }
    }

    override suspend fun getCachedLoads(): List<Load> {
        logger.info(LogCategory.GENERAL, "💾 LoadRepositoryImpl: Getting cached loads only")
        val loads = loadsLocalDataSource.getLoads()
        val result = loads.map { loadEntity ->
            val stops = stopsLocalDataSource.getStopsByLoadId(loadEntity.id)
                .map { it.toDomain() }
            loadEntity.toDomain().copy(stops = stops)
        }
        return result
    }

    override suspend fun getLoadById(loadId: Long): Load? {
        logger.info(LogCategory.GENERAL, "💾 LoadRepositoryImpl: Getting load by id=$loadId")
        val loadEntity = loadsLocalDataSource.getLoadById(loadId) ?: return null

        val stops = stopsLocalDataSource.getStopsByLoadId(loadEntity.id)
            .map { it.toDomain() }

        return loadEntity.toDomain().copy(stops = stops)
    }

    override suspend fun getStopsByLoadId(loadId: Long): List<Stop> {
        return stopsLocalDataSource.getStopsByLoadId(loadId).map { it.toDomain() }
    }

    override suspend fun getNotEnteredStopsByLoadId(loadId: Long): List<Stop> {
        logger.info(LogCategory.GENERAL, "💾 LoadRepositoryImpl: Getting stops for load $loadId where enter == 0")
        return stopsLocalDataSource.getNotEnteredStopsByLoad(loadId).map { it.toDomain() }
    }

    override suspend fun getConnectedLoad(): Load? {
        return getCachedLoads().find { it.loadStatus == LoadStatus.LOAD_STATUS_CONNECTED }
    }

    override suspend fun connectToLoad(
        token: String,
        serverLoadId: Long,
    ): Result<List<Load>> {
        logger.info(LogCategory.GENERAL, "🔄 LoadRepositoryImpl: Connecting to load $serverLoadId")

        return try {
            logger.info(LogCategory.GENERAL, "🌐 LoadRepositoryImpl: Sending connect request to server")
            val loadDtos = loadsRemoteDataSource.connectToLoad(token, serverLoadId)

            // Cache the updated results
            logger.info(LogCategory.GENERAL, "💾 LoadRepositoryImpl: Updating cache with ${loadDtos.size} loads")

            saveLoads(loadDtos)

            // Return domain models
            val loads = getCachedLoads()
            logger.info(LogCategory.GENERAL, "✅ LoadRepositoryImpl: Successfully connected to load $serverLoadId")
            Result.success(loads)
        } catch (e: Exception) {
            logger.info(LogCategory.GENERAL, "❌ LoadRepositoryImpl: Failed to connect to load: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun disconnectFromLoad(
        token: String,
        serverLoadId: Long,
    ): Result<List<Load>> {
        logger.info(LogCategory.GENERAL, "🔄 LoadRepositoryImpl: Disconnecting from load $serverLoadId")

        return try {
            logger.info(LogCategory.GENERAL, "🌐 LoadRepositoryImpl: Sending disconnect request to server")
            val loadDtos = loadsRemoteDataSource.disconnectFromLoad(token, serverLoadId)

            // Cache the updated results
            logger.info(LogCategory.GENERAL, "💾 LoadRepositoryImpl: Updating cache with ${loadDtos.size} loads")
            saveLoads(loadDtos)

            // Return domain models
            val loads = getCachedLoads()
            logger.info(LogCategory.GENERAL, "✅ LoadRepositoryImpl: Successfully disconnected from load $serverLoadId")
            Result.success(loads)
        } catch (e: Exception) {
            logger.info(LogCategory.GENERAL, "❌ LoadRepositoryImpl: Failed to disconnect from load: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun rejectLoad(
        token: String,
        serverLoadId: Long,
    ): Result<List<Load>> {
        logger.info(LogCategory.GENERAL, "🔄 LoadRepositoryImpl: Rejecting load $serverLoadId")

        return try {
            logger.info(LogCategory.GENERAL, "🌐 LoadRepositoryImpl: Sending reject request to server")
            val loadDtos = loadsRemoteDataSource.rejectLoad(token, serverLoadId)

            // Cache the updated results
            logger.info(LogCategory.GENERAL, "💾 LoadRepositoryImpl: Updating cache with ${loadDtos.size} loads")
            saveLoads(loadDtos)

            // Return domain models
            val loads = getCachedLoads()
            logger.info(LogCategory.GENERAL, "✅ LoadRepositoryImpl: Successfully rejected load $serverLoadId")
            Result.success(loads)
        } catch (e: Exception) {
            logger.info(LogCategory.GENERAL, "❌ LoadRepositoryImpl: Failed to reject load: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun pingLoad(
        token: String,
        serverLoadId: Long,
    ): Result<Unit> {
        logger.info(LogCategory.GENERAL, "🔄 LoadRepositoryImpl: Pinging load $serverLoadId")

        return try {
            logger.info(LogCategory.GENERAL, "🌐 LoadRepositoryImpl: Sending ping request to server")
            loadsRemoteDataSource.pingLoad(token, serverLoadId)

            logger.info(LogCategory.GENERAL, "✅ LoadRepositoryImpl: Successfully pinged load $serverLoadId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.info(LogCategory.GENERAL, "❌ LoadRepositoryImpl: Failed to ping load: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun addStopIdToQueue(stopId: Long) {
        logger.info(LogCategory.GENERAL, "💾 LoadRepositoryImpl: Adding stopId $stopId to queue")
        loadsLocalDataSource.addStopIdToQueue(stopId)
    }

    override suspend fun sendEnterStopQueue(token: String): Result<Unit> {
        logger.info(LogCategory.GENERAL, "🔄 LoadRepositoryImpl: Sending enter stop queue to server")

        return try {
            // Get all queued stop IDs
            val queuedStopIds = loadsLocalDataSource.getQueuedStopIds()

            if (queuedStopIds.isEmpty()) {
                logger.info(LogCategory.GENERAL, "📭 LoadRepositoryImpl: No stop IDs in queue")
                return Result.success(Unit)
            }

            logger.info(LogCategory.GENERAL, "📤 LoadRepositoryImpl: Sending ${queuedStopIds.size} stop IDs to server")

            // Send each stop ID to server
            val successfullySent = mutableListOf<Long>()
            val failed = mutableListOf<Long>()

            queuedStopIds.forEach { stopId ->
                try {
                    val success = loadsRemoteDataSource.enterStop(token, stopId)
                    if (success) {
                        successfullySent.add(stopId)
                        logger.info(LogCategory.GENERAL, "✅ LoadRepositoryImpl: Successfully sent stopId $stopId")
                    } else {
                        failed.add(stopId)
                        logger.warn(LogCategory.GENERAL, "⚠️ LoadRepositoryImpl: Failed to send stopId $stopId (non-success status)")
                    }
                } catch (e: Exception) {
                    failed.add(stopId)
                    logger.error(LogCategory.GENERAL, "❌ LoadRepositoryImpl: Error sending stopId $stopId: ${e.message}", e)
                }
            }

            // Remove successfully sent stop IDs from queue
            if (successfullySent.isNotEmpty()) {
                loadsLocalDataSource.removeStopIdsFromQueue(successfullySent)
                logger.info(
                    LogCategory.GENERAL,
                    "🗑️ LoadRepositoryImpl: Removed ${successfullySent.size} successfully sent stop IDs from queue"
                )
            }

            if (failed.isNotEmpty()) {
                logger.warn(LogCategory.GENERAL, "⚠️ LoadRepositoryImpl: ${failed.size} stop IDs failed to send and remain in queue")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "❌ LoadRepositoryImpl: Failed to send enter stop queue: ${e.message}", e)
            Result.failure(e)
        }
    }

    override fun observeNotEnteredStopIdsUpdates(): Flow<List<Stop>> {
        logger.info(LogCategory.GENERAL, "🔄 LoadRepositoryImpl: Observing not entered stops")
        return loadsLocalDataSource.observeNotEnteredStops()
            .map { stopEntities -> stopEntities.map { it.toDomain() } }
    }

    private suspend fun saveLoads(loadDtos: List<LoadDto>) {
        // Get all existing loads from database
        val existingLoads = loadsLocalDataSource.getLoads()
        val existingServerIds = existingLoads.map { it.serverId }.toSet()
        val newServerIds = loadDtos.map { it.id }.toSet()

        // Find loads to delete (exist in database but not in loadDtos)
        val serverIdsToDelete = existingServerIds - newServerIds
        if (serverIdsToDelete.isNotEmpty()) {
            logger.info(
                LogCategory.GENERAL,
                "💾 LoadRepositoryImpl: Deleting ${serverIdsToDelete.size} loads that are not in server response"
            )
            loadsLocalDataSource.deleteLoadsNotIn(newServerIds.toList())
        }

        // Separate loads into new and existing
        val loadsToInsert = mutableListOf<LoadEntity>()
        val loadsToUpdate = mutableListOf<LoadEntity>()

        loadDtos.forEach { loadDto ->
            val existingLoad = existingLoads.find { it.serverId == loadDto.id }
            if (existingLoad != null) {
                // Update existing load with new data, keeping the same id
                loadsToUpdate.add(loadDto.toEntity().copy(id = existingLoad.id))
            } else {
                // New load: set id = 0 so Room will auto-generate a new id
                loadsToInsert.add(loadDto.toEntity().copy(id = 0))
            }
        }

        // Insert new loads
        if (loadsToInsert.isNotEmpty()) {
            logger.info(LogCategory.GENERAL, "💾 LoadRepositoryImpl: Inserting ${loadsToInsert.size} new loads")
            loadsLocalDataSource.insertLoads(loadsToInsert)
        }

        // Update existing loads
        if (loadsToUpdate.isNotEmpty()) {
            logger.info(LogCategory.GENERAL, "💾 LoadRepositoryImpl: Updating ${loadsToUpdate.size} existing loads")
            loadsLocalDataSource.updateLoads(loadsToUpdate)
        }

        // Get all load entities (including newly inserted ones) for stops mapping
        val allLoads = loadsLocalDataSource.getLoads()
        val loadEntities = loadDtos.map { loadDto ->
            allLoads.find { it.serverId == loadDto.id } ?: error("Load not found after save: ${loadDto.id}")
        }

        // Cache stops for each load - используем loadEntity.id для связи
        loadDtos.zip(loadEntities).forEach { (loadDto, loadEntity) ->
            // Get existing stops for this load
            val existingStops = stopsLocalDataSource.getStopsByLoadId(loadEntity.id)
            val existingServerIds = existingStops.map { it.serverId }.toSet()
            val newServerIds = loadDto.stops.map { it.id }.toSet()

            // Find stops to delete (exist in database but not in loadDto.stops)
            val serverIdsToDelete = existingServerIds - newServerIds
            if (serverIdsToDelete.isNotEmpty() || newServerIds.isEmpty()) {
                logger.info(
                    LogCategory.GENERAL,
                    "💾 LoadRepositoryImpl: Deleting stops for load ${loadEntity.id} that are not in server response",
                )
                stopsLocalDataSource.deleteStopsNotIn(loadEntity.id, newServerIds.toList())
            }

            // Separate stops into new and existing
            val stopsToInsert = mutableListOf<StopEntity>()
            val stopsToUpdate = mutableListOf<StopEntity>()

            loadDto.stops.forEach { stopDto ->
                val existingStop = existingStops.find { it.serverId == stopDto.id }
                if (existingStop != null) {
                    // Update existing stop with new data, keeping the same id
                    stopsToUpdate.add(stopDto.toStopEntity(loadEntity.id).copy(id = existingStop.id))
                } else {
                    // New stop: set id = 0 so Room will auto-generate a new id
                    stopsToInsert.add(stopDto.toStopEntity(loadEntity.id).copy(id = 0))
                }
            }

            // Insert new stops
            if (stopsToInsert.isNotEmpty()) {
                logger.info(
                    LogCategory.GENERAL,
                    "💾 LoadRepositoryImpl: Inserting ${stopsToInsert.size} new stops for load ${loadEntity.id}",
                )
                stopsLocalDataSource.insertStops(stopsToInsert)
            }

            // Update existing stops
            if (stopsToUpdate.isNotEmpty()) {
                logger.info(
                    LogCategory.GENERAL,
                    "💾 LoadRepositoryImpl: Updating ${stopsToUpdate.size} existing stops for load ${loadEntity.id}",
                )
                stopsLocalDataSource.updateStops(stopsToUpdate)
            }
        }
    }

    override suspend fun updateStopCompletion(
        token: String,
        stopId: Long,
        completion: Int,
    ): Result<Stop> {
        logger.info(LogCategory.GENERAL, "🔄 LoadRepositoryImpl: Updating stop completion for stop $stopId to $completion")
        
        return try {
            val stopDto = loadsRemoteDataSource.updateStopCompletion(token, stopId, completion)
            
            // Update stop in local database
            val stopEntity = stopDto.toStopEntity(0) // loadId will be updated from existing stop
            val existingStop = stopsLocalDataSource.getStopByServerId(stopId)
            
            if (existingStop != null) {
                val updatedStop = stopEntity.copy(
                    id = existingStop.id,
                    loadId = existingStop.loadId,
                )
                stopsLocalDataSource.updateStops(listOf(updatedStop))
                logger.info(LogCategory.GENERAL, "✅ LoadRepositoryImpl: Successfully updated stop completion")
                Result.success(updatedStop.toDomain())
            } else {
                logger.warn(LogCategory.GENERAL, "⚠️ LoadRepositoryImpl: Stop not found in local database")
                Result.success(stopDto.toDomain())
            }
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "❌ LoadRepositoryImpl: Failed to update stop completion: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun clearAllData() {
        logger.info(LogCategory.GENERAL, "🔄 LoadRepositoryImpl: Clearing all data from database")
        try {
            loadsLocalDataSource.removeLoads()
            stopsLocalDataSource.clearCache()
            logger.info(LogCategory.GENERAL, "✅ LoadRepositoryImpl: Successfully cleared all data")
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "❌ LoadRepositoryImpl: Failed to clear data: ${e.message}")
            throw e
        }
    }
}
