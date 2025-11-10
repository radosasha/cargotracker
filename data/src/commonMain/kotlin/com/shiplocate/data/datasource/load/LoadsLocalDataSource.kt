package com.shiplocate.data.datasource.load

import com.shiplocate.core.database.TrackerDatabase
import com.shiplocate.core.database.entity.EnterStopQueueEntity
import com.shiplocate.core.database.entity.LoadEntity
import kotlinx.coroutines.flow.Flow

/**
 * Local data source for Load caching
 * Handles database operations through Room
 */
class LoadsLocalDataSource(
    private val database: TrackerDatabase,
    private val stopsLocalDataSource: StopsLocalDataSource,
) {
    private val loadDao = database.loadDao()
    private val enterStopQueueDao = database.enterStopQueueDao()

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
     * Get load by server ID from cache
     */
    suspend fun getLoadByServerId(serverId: Long): LoadEntity? {
        println("💾 LoadLocalDataSource: Getting load by serverId=$serverId")
        return loadDao.getLoadByServerId(serverId)
    }

    /**
     * Insert new loads to database
     */
    suspend fun insertLoads(loads: List<LoadEntity>) {
        println("💾 LoadLocalDataSource: Inserting ${loads.size} loads")
        loadDao.insertLoads(loads)
    }

    /**
     * Update existing loads in database
     */
    suspend fun updateLoads(loads: List<LoadEntity>) {
        println("💾 LoadLocalDataSource: Updating ${loads.size} loads")
        loadDao.updateLoads(loads)
    }

    /**
     * Clear all cached loads
     */
    suspend fun removeLoads() {
        println("💾 LoadLocalDataSource: Clearing cache")
        loadDao.deleteAll()
    }

    /**
     * Delete loads that are not in the provided serverIds list
     */
    suspend fun deleteLoadsNotIn(notInServerIds: List<Long>) {
        if (notInServerIds.isNotEmpty()) {
            println("💾 LoadLocalDataSource: Deleting loads not in serverIds list")
            loadDao.deleteLoadsNotIn(notInServerIds)
        } else {
            // If serverIds is empty, delete all loads
            println("💾 LoadLocalDataSource: ServerIds list is empty, deleting all loads")
            loadDao.deleteAll()
        }
    }

    /**
     * Add stop ID to enter stop queue if it doesn't exist
     */
    suspend fun addStopIdToQueue(stopId: Long) {
        val existing = enterStopQueueDao.getByStopId(stopId)
        if (existing == null) {
            println("💾 LoadLocalDataSource: Adding stopId $stopId to enter stop queue")
            enterStopQueueDao.insertStopId(EnterStopQueueEntity(stopId = stopId))
        } else {
            println("💾 LoadLocalDataSource: StopId $stopId already in queue, skipping")
        }
    }

    /**
     * Get all queued stop IDs
     */
    suspend fun getQueuedStopIds(): List<Long> {
        println("💾 LoadLocalDataSource: Getting all queued stop IDs")
        return enterStopQueueDao.getAllQueuedStops().map { it.stopId }
    }

    /**
     * Remove stop IDs from queue
     */
    suspend fun removeStopIdsFromQueue(stopIds: List<Long>) {
        if (stopIds.isNotEmpty()) {
            println("💾 LoadLocalDataSource: Removing ${stopIds.size} stop IDs from queue")
            enterStopQueueDao.deleteByStopIds(stopIds)
        }
    }

    /**
     * Observe stops where enter == 0
     * Returns Flow that emits list of StopEntity when stops with enter=0 change
     */
    fun observeNotEnteredStops(): Flow<List<com.shiplocate.core.database.entity.StopEntity>> {
        println("💾 LoadLocalDataSource: Observing not entered stops")
        return stopsLocalDataSource.observeNotEnteredStops()
    }
}
