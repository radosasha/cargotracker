package com.shiplocate.data.datasource.load

import com.shiplocate.core.database.TrackerDatabase
import com.shiplocate.core.database.entity.StopEntity
import kotlinx.coroutines.flow.Flow

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
     * Get stops for a specific load where enter == 0
     */
    suspend fun getNotEnteredStopsByLoad(loadId: Long): List<StopEntity> {
        println("💾 StopsLocalDataSource: Getting stops for load $loadId where enter == 0")
        return stopDao.getNotEnteredStopsByLoad(loadId)
    }

    /**
     * Insert new stops to database
     */
    suspend fun insertStops(stops: List<StopEntity>) {
        println("💾 StopsLocalDataSource: Inserting ${stops.size} stops")
        stopDao.insertStops(stops)
    }

    /**
     * Update existing stops in database
     */
    suspend fun updateStops(stops: List<StopEntity>) {
        println("💾 StopsLocalDataSource: Updating ${stops.size} stops")
        stopDao.updateStops(stops)
    }

    /**
     * Delete stops for a specific load that are not in the provided serverIds list
     */
    suspend fun deleteStopsNotIn(loadId: Long, notInServerIds: List<Long>) {
        if (notInServerIds.isNotEmpty()) {
            println("💾 StopsLocalDataSource: Deleting stops for load $loadId not in serverIds list")
            stopDao.deleteStopsNotIn(loadId, notInServerIds)
        } else {
            // If serverIds is empty, delete all stops for this load
            println("💾 StopsLocalDataSource: ServerIds list is empty, deleting all stops for load $loadId")
            stopDao.deleteStopsByLoadId(loadId)
        }
    }

    /**
     * Clear all cached stops
     */
    suspend fun clearCache() {
        println("💾 StopsLocalDataSource: Clearing cache")
        stopDao.deleteAll()
    }

    /**
     * Observe stops where enter == 0
     * Returns Flow that emits list of StopEntity when stops with enter=0 change
     */
    fun observeNotEnteredStops(): Flow<List<StopEntity>> {
        println("💾 StopsLocalDataSource: Observing not entered stops")
        return stopDao.observeNotEnteredStops()
    }
}

