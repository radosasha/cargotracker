package com.tracker.data.repository

import com.tracker.data.datasource.load.LoadLocalDataSource
import com.tracker.data.datasource.load.LoadRemoteDataSource
import com.tracker.data.mapper.toDomain
import com.tracker.data.mapper.toEntity
import com.tracker.domain.model.load.Load
import com.tracker.domain.repository.LoadRepository

/**
 * Implementation of LoadRepository
 * Handles fetching loads from server with automatic caching fallback
 */
class LoadRepositoryImpl(
    private val remoteDataSource: LoadRemoteDataSource,
    private val localDataSource: LoadLocalDataSource
) : LoadRepository {
    
    override suspend fun getLoads(token: String): Result<List<Load>> {
        println("🔄 LoadRepositoryImpl: Getting loads with token")
        
        return try {
            // Try to fetch from server
            println("🌐 LoadRepositoryImpl: Fetching from server")
            val loadDtos = remoteDataSource.getLoads(token)
            
            // Cache the results
            println("💾 LoadRepositoryImpl: Remove previous cached loads")
            clearCache()
            println("💾 LoadRepositoryImpl: Caching ${loadDtos.size} loads")
            localDataSource.cacheLoads(loadDtos.map { it.toEntity() })
            
            // Return domain models
            val loads = loadDtos.map { it.toDomain() }
            println("✅ LoadRepositoryImpl: Successfully loaded ${loads.size} loads from server")
            Result.success(loads)
            
        } catch (e: Exception) {
            // Server failed, try cache
            println("⚠️ LoadRepositoryImpl: Server request failed, falling back to cache: ${e.message}")
            
            try {
                val cachedLoads = localDataSource.getCachedLoads().map { it.toDomain() }
                if (cachedLoads.isNotEmpty()) {
                    println("✅ LoadRepositoryImpl: Loaded ${cachedLoads.size} loads from cache")
                    Result.success(cachedLoads)
                } else {
                    println("❌ LoadRepositoryImpl: No cached loads available")
                    Result.failure(Exception("No cached data available. Please check your connection."))
                }
            } catch (cacheError: Exception) {
                println("❌ LoadRepositoryImpl: Cache read failed: ${cacheError.message}")
                Result.failure(Exception("Failed to load data: ${e.message}"))
            }
        }
    }
    
    override suspend fun getCachedLoads(): List<Load> {
        println("💾 LoadRepositoryImpl: Getting cached loads only")
        return localDataSource.getCachedLoads().map { it.toDomain() }
    }

    override suspend fun getConnectedLoad(): Load? {
        return getCachedLoads().find { it.loadStatus == 1 }
    }
    
    override suspend fun clearCache() {
        println("🗑️ LoadRepositoryImpl: Clearing cache")
        localDataSource.clearCache()
    }
    
    override suspend fun connectToLoad(token: String, loadId: String): Result<List<Load>> {
        println("🔄 LoadRepositoryImpl: Connecting to load $loadId")
        
        return try {
            println("🌐 LoadRepositoryImpl: Sending connect request to server")
            val loadDtos = remoteDataSource.connectToLoad(token, loadId)
            
            // Cache the updated results
            println("💾 LoadRepositoryImpl: Updating cache with ${loadDtos.size} loads")
            localDataSource.cacheLoads(loadDtos.map { it.toEntity() })
            
            // Return domain models
            val loads = loadDtos.map { it.toDomain() }
            println("✅ LoadRepositoryImpl: Successfully connected to load $loadId")
            Result.success(loads)
            
        } catch (e: Exception) {
            println("❌ LoadRepositoryImpl: Failed to connect to load: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun disconnectFromLoad(token: String, loadId: String): Result<List<Load>> {
        println("🔄 LoadRepositoryImpl: Disconnecting from load $loadId")
        
        return try {
            println("🌐 LoadRepositoryImpl: Sending disconnect request to server")
            val loadDtos = remoteDataSource.disconnectFromLoad(token, loadId)
            
            // Cache the updated results
            println("💾 LoadRepositoryImpl: Updating cache with ${loadDtos.size} loads")
            localDataSource.cacheLoads(loadDtos.map { it.toEntity() })
            
            // Return domain models
            val loads = loadDtos.map { it.toDomain() }
            println("✅ LoadRepositoryImpl: Successfully disconnected from load $loadId")
            Result.success(loads)
            
        } catch (e: Exception) {
            println("❌ LoadRepositoryImpl: Failed to disconnect from load: ${e.message}")
            Result.failure(e)
        }
    }
}

