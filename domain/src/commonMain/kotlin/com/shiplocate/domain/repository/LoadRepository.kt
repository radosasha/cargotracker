package com.shiplocate.domain.repository

import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.model.load.Stop
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Load data operations
 * Handles both remote API calls and local caching
 */
interface LoadRepository {
    /**
     * Get loads from remote server with fallback to cache
     * @param token Authentication token
     * @return List of loads, either from server or cache
     */
    suspend fun getLoads(token: String): Result<List<Load>>

    /**
     * Get cached loads from local database
     * @return List of cached loads
     */
    suspend fun getCachedLoads(): List<Load>

    /**
     * Get load by internal ID from cache
     * @param loadId Internal load ID
     * @return Load if found, null otherwise
     */
    suspend fun getLoadById(loadId: Long): Load?

    suspend fun getStopsByLoadId(loadId: Long): List<Stop>

    /**
     * Get stops for a specific load where enter == 0
     * @param loadId Internal load ID
     * @return List of stops where enter == 0
     */
    suspend fun getNotEnteredStopsByLoadId(loadId: Long): List<Stop>

    suspend fun getConnectedLoad(): Load?

    /**
     * Connect to load
     * Sets loadstatus=1 for the specified load and loadstatus=2 for all other loads with loadstatus=1
     * @param token Authentication token
     * @param serverLoadId Load ID to connect to
     * @return Updated list of loads
     */
    suspend fun connectToLoad(
        token: String,
        serverLoadId: Long,
    ): Result<List<Load>>

    /**
     * Disconnect from load
     * Sets loadstatus=2 for the specified load
     * @param token Authentication token
     * @param serverLoadId Load ID to disconnect from
     * @return Updated list of loads
     */
    suspend fun disconnectFromLoad(
        token: String,
        serverLoadId: Long,
    ): Result<List<Load>>

    /**
     * Reject load
     * Rejects the specified load
     * @param token Authentication token
     * @param serverLoadId Load ID to reject
     * @return Updated list of loads
     */
    suspend fun rejectLoad(
        token: String,
        serverLoadId: Long,
    ): Result<List<Load>>

    /**
     * Ping load to update connection status
     * Updates connectionStatus to "online" and lastUpdate timestamp
     * @param token Authentication token
     * @param serverLoadId Load ID to ping
     */
    suspend fun pingLoad(
        token: String,
        serverLoadId: Long,
    ): Result<Unit>

    /**
     * Add stop ID to enter stop queue if it doesn't exist
     * @param stopId Server's stop ID
     */
    suspend fun addStopIdToQueue(stopId: Long)

    /**
     * Send all queued stop IDs to server via enterstop API
     * Removes successfully sent stop IDs from queue
     * @param token Authentication token
     * @return Result indicating success or failure
     */
    suspend fun sendEnterStopQueue(token: String): Result<Unit>

    /**
     * Observe stops where enter == 0
     * Returns Flow that emits list of stops when stops with enter=0 change
     * @return Flow of stops where enter == 0
     */
    fun observeNotEnteredStopIdsUpdates(): Flow<List<Stop>>

    /**
     * Update stop completion status
     * Updates the completion field for a stop
     * @param token Authentication token
     * @param stopId Server's stop ID
     * @param completion Completion status (0 = NOT_COMPLETED, 1 = COMPLETED)
     * @return Updated Stop
     */
    suspend fun updateStopCompletion(
        token: String,
        stopId: Long,
        completion: Int,
    ): Result<Stop>

    /**
     * Clear all data from database (loads, stops, etc.)
     * Used during logout to remove all cached data
     */
    suspend fun clearAllData()
}
