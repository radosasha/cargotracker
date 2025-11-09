package com.shiplocate.data.datasource.load

import com.shiplocate.core.database.TrackerDatabase
import com.shiplocate.core.database.entity.StopEntity

/**
 * Local data source for Stop caching
 * Handles database operations through Room
 */
class StopsLocalDataSource(
    private val database: TrackerDatabase,
) {
    private val stopDao = database.stopDao()

    /**
     * Get all stops for a specific load
     */
    suspend fun getStopsByLoadId(loadId: Long): List<StopEntity> {
        println("💾 StopsLocalDataSource: Getting stops for load $loadId")
        return stopDao.getStopsByLoadId(loadId)
    }

    /**
     * Cache stops to database
     */
    suspend fun saveStops(stops: List<StopEntity>) {
        println("💾 StopsLocalDataSource: Caching ${stops.size} stops")
        stopDao.insertStops(stops)
    }

    /**
     * Clear all cached stops
     */
    suspend fun clearCache() {
        println("💾 StopsLocalDataSource: Clearing cache")
        stopDao.deleteAll()
    }
}

