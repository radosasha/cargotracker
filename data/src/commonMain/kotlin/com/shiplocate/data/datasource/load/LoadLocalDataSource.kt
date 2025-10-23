package com.shiplocate.data.datasource.load

import com.shiplocate.core.database.TrackerDatabase
import com.shiplocate.core.database.entity.LoadEntity

/**
 * Local data source for Load caching
 * Handles database operations through Room
 */
class LoadLocalDataSource(
    private val database: TrackerDatabase,
) {
    private val loadDao = database.loadDao()

    /**
     * Get all cached loads
     */
    suspend fun getCachedLoads(): List<LoadEntity> {
        println("💾 LoadLocalDataSource: Getting cached loads")
        return loadDao.getAllLoads()
    }

    /**
     * Cache loads to database
     */
    suspend fun cacheLoads(loads: List<LoadEntity>) {
        println("💾 LoadLocalDataSource: Caching ${loads.size} loads")
        loadDao.insertLoads(loads)
    }

    /**
     * Clear all cached loads
     */
    suspend fun clearCache() {
        println("💾 LoadLocalDataSource: Clearing cache")
        loadDao.deleteAll()
    }
}
