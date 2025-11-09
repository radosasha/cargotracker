package com.shiplocate.data.repository

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.load.LoadsLocalDataSource
import com.shiplocate.data.datasource.load.LoadsRemoteDataSource
import com.shiplocate.data.datasource.load.StopsLocalDataSource
import com.shiplocate.data.mapper.toDomain
import com.shiplocate.data.mapper.toEntity
import com.shiplocate.data.mapper.toStopEntities
import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.repository.LoadRepository

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

            // Delete Loads and cascade delete stops
            loadsLocalDataSource.removeLoads()

            logger.info(LogCategory.GENERAL, "💾 LoadRepositoryImpl: Caching ${loadDtos.size} loads")
            val loadEntities = loadDtos.map { it.toEntity() }
            loadsLocalDataSource.saveLoads(loadEntities)

            // Cache stops for each load - используем loadEntity.id для связи
            loadDtos.zip(loadEntities).forEach { (loadDto, loadEntity) ->
                val stops = loadDto.toStopEntities(loadEntity.id)
                if (stops.isNotEmpty()) {
                    logger.info(LogCategory.GENERAL, "💾 LoadRepositoryImpl: Caching ${stops.size} stops for load ${loadEntity.id}")
                    stopsLocalDataSource.saveStops(stops)
                }
            }

            // Return domain models
            val loads = loadDtos.map { it.toDomain() }
            logger.info(LogCategory.GENERAL, "✅ LoadRepositoryImpl: Successfully loaded ${loads.size} loads from server")
            Result.success(loads)
        } catch (e: Exception) {
            // Server failed, try cache
            logger.info(LogCategory.GENERAL, "⚠️ LoadRepositoryImpl: Server request failed, falling back to cache: ${e.message}")

            try {
                val loads = loadsLocalDataSource.getLoads().map { loadEntity ->
                    val stops = stopsLocalDataSource.getStopsByLoadId(loadEntity.id)
                        .map { it.toDomain() }
                    loadEntity.toDomain().copy(stops = stops)
                }
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
        return loadsLocalDataSource.getLoads().map { loadEntity ->
            val stops = stopsLocalDataSource.getStopsByLoadId(loadEntity.id)
                .map { it.toDomain() }
            loadEntity.toDomain().copy(stops = stops)
        }
    }

    override suspend fun getConnectedLoad(): Load? {
        return getCachedLoads().find { it.loadStatus == 1 }
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
            val loadEntities = loadDtos.map { it.toEntity() }
            loadsLocalDataSource.saveLoads(loadEntities)

            // Cache stops for each load - используем loadEntity.id для связи
            loadDtos.zip(loadEntities).forEach { (loadDto, loadEntity) ->
                val stops = loadDto.toStopEntities(loadEntity.id)
                if (stops.isNotEmpty()) {
                    stopsLocalDataSource.saveStops(stops)
                }
            }

            // Return domain models
            val loads = loadDtos.map { it.toDomain() }
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
            val loadEntities = loadDtos.map { it.toEntity() }
            loadsLocalDataSource.saveLoads(loadEntities)

            // Cache stops for each load - используем loadEntity.id для связи
            loadDtos.zip(loadEntities).forEach { (loadDto, loadEntity) ->
                val stops = loadDto.toStopEntities(loadEntity.id)
                if (stops.isNotEmpty()) {
                    stopsLocalDataSource.saveStops(stops)
                }
            }

            // Return domain models
            val loads = loadDtos.map { it.toDomain() }
            logger.info(LogCategory.GENERAL, "✅ LoadRepositoryImpl: Successfully disconnected from load $serverLoadId")
            Result.success(loads)
        } catch (e: Exception) {
            logger.info(LogCategory.GENERAL, "❌ LoadRepositoryImpl: Failed to disconnect from load: ${e.message}")
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
}
