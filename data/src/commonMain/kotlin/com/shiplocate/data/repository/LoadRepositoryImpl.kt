package com.shiplocate.data.repository

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.load.LoadLocalDataSource
import com.shiplocate.data.datasource.load.LoadRemoteDataSource
import com.shiplocate.data.mapper.toDomain
import com.shiplocate.data.mapper.toEntity
import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.repository.LoadRepository

/**
 * Implementation of LoadRepository
 * Handles fetching loads from server with automatic caching fallback
 */
class LoadRepositoryImpl(
    private val remoteDataSource: LoadRemoteDataSource,
    private val localDataSource: LoadLocalDataSource,
    private val logger: Logger,
) : LoadRepository {
    override suspend fun getLoads(token: String): Result<List<Load>> {
        logger.info(LogCategory.GENERAL, "🔄 LoadRepositoryImpl: Getting loads with token")

        return try {
            // Try to fetch from server
            logger.info(LogCategory.GENERAL, "🌐 LoadRepositoryImpl: Fetching from server")
            val loadDtos = remoteDataSource.getLoads(token)

            // Cache the results
            logger.info(LogCategory.GENERAL, "💾 LoadRepositoryImpl: Remove previous cached loads")
            clearCache()
            logger.info(LogCategory.GENERAL, "💾 LoadRepositoryImpl: Caching ${loadDtos.size} loads")
            localDataSource.cacheLoads(loadDtos.map { it.toEntity() })

            // Return domain models
            val loads = loadDtos.map { it.toDomain() }
            logger.info(LogCategory.GENERAL, "✅ LoadRepositoryImpl: Successfully loaded ${loads.size} loads from server")
            Result.success(loads)
        } catch (e: Exception) {
            // Server failed, try cache
            logger.info(LogCategory.GENERAL, "⚠️ LoadRepositoryImpl: Server request failed, falling back to cache: ${e.message}")

            try {
                val cachedLoads = localDataSource.getCachedLoads().map { it.toDomain() }
                if (cachedLoads.isNotEmpty()) {
                    logger.info(LogCategory.GENERAL, "✅ LoadRepositoryImpl: Loaded ${cachedLoads.size} loads from cache")
                    Result.success(cachedLoads)
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
        return localDataSource.getCachedLoads().map { it.toDomain() }
    }

    override suspend fun getConnectedLoad(): Load? {
        return getCachedLoads().find { it.loadStatus == 1 }
    }

    override suspend fun clearCache() {
        logger.info(LogCategory.GENERAL, "🗑️ LoadRepositoryImpl: Clearing cache")
        localDataSource.clearCache()
    }

    override suspend fun connectToLoad(
        token: String,
        loadId: String,
    ): Result<List<Load>> {
        logger.info(LogCategory.GENERAL, "🔄 LoadRepositoryImpl: Connecting to load $loadId")

        return try {
            logger.info(LogCategory.GENERAL, "🌐 LoadRepositoryImpl: Sending connect request to server")
            val loadDtos = remoteDataSource.connectToLoad(token, loadId)

            // Cache the updated results
            logger.info(LogCategory.GENERAL, "💾 LoadRepositoryImpl: Updating cache with ${loadDtos.size} loads")
            localDataSource.cacheLoads(loadDtos.map { it.toEntity() })

            // Return domain models
            val loads = loadDtos.map { it.toDomain() }
            logger.info(LogCategory.GENERAL, "✅ LoadRepositoryImpl: Successfully connected to load $loadId")
            Result.success(loads)
        } catch (e: Exception) {
            logger.info(LogCategory.GENERAL, "❌ LoadRepositoryImpl: Failed to connect to load: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun disconnectFromLoad(
        token: String,
        loadId: String,
    ): Result<List<Load>> {
        logger.info(LogCategory.GENERAL, "🔄 LoadRepositoryImpl: Disconnecting from load $loadId")

        return try {
            logger.info(LogCategory.GENERAL, "🌐 LoadRepositoryImpl: Sending disconnect request to server")
            val loadDtos = remoteDataSource.disconnectFromLoad(token, loadId)

            // Cache the updated results
            logger.info(LogCategory.GENERAL, "💾 LoadRepositoryImpl: Updating cache with ${loadDtos.size} loads")
            localDataSource.cacheLoads(loadDtos.map { it.toEntity() })

            // Return domain models
            val loads = loadDtos.map { it.toDomain() }
            logger.info(LogCategory.GENERAL, "✅ LoadRepositoryImpl: Successfully disconnected from load $loadId")
            Result.success(loads)
        } catch (e: Exception) {
            logger.info(LogCategory.GENERAL, "❌ LoadRepositoryImpl: Failed to disconnect from load: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun pingLoad(
        token: String,
        loadId: String,
    ): Result<Unit> {
        logger.info(LogCategory.GENERAL, "🔄 LoadRepositoryImpl: Pinging load $loadId")

        return try {
            logger.info(LogCategory.GENERAL, "🌐 LoadRepositoryImpl: Sending ping request to server")
            remoteDataSource.pingLoad(token, loadId)

            logger.info(LogCategory.GENERAL, "✅ LoadRepositoryImpl: Successfully pinged load $loadId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.info(LogCategory.GENERAL, "❌ LoadRepositoryImpl: Failed to ping load: ${e.message}")
            Result.failure(e)
        }
    }
}
