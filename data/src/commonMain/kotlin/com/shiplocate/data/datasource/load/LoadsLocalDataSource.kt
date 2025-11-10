package com.shiplocate.data.datasource.load

import com.shiplocate.core.database.TrackerDatabase
import com.shiplocate.core.database.entity.LoadEntity

/**
 * Local data source for Load caching
 * Handles database operations through Room
 */
class LoadsLocalDataSource(
    private val database: TrackerDatabase,
) {
    private val loadDao = database.loadDao()

    /**
     * Get all cached loads
     */
    suspend fun getLoads(): List<LoadEntity> {
        println("💾 LoadLocalDataSource: Getting cached loads")
        return loadDao.getAllLoads()
    }

    /**
     * Get load by internal ID from cache
     */
    suspend fun getLoadById(loadId: Long): LoadEntity? {
        println("💾 LoadLocalDataSource: Getting load by id=$loadId")
        return loadDao.getLoadById(loadId)
    }

    /**
     * Cache loads to database
     */
    suspend fun saveLoads(loads: List<LoadEntity>) {
        println("💾 LoadLocalDataSource: Caching ${loads.size} loads")
        loadDao.insertLoads(loads)
    }

    /**
     * Clear all cached loads
     */
    suspend fun removeLoads() {
        println("💾 LoadLocalDataSource: Clearing cache")
        loadDao.deleteAll()
    }
}
