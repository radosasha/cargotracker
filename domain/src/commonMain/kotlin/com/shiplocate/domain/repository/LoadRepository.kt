package com.shiplocate.domain.repository

import com.shiplocate.domain.model.load.Load

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
     * Ping load to update connection status
     * Updates connectionStatus to "online" and lastUpdate timestamp
     * @param token Authentication token
     * @param serverLoadId Load ID to ping
     */
    suspend fun pingLoad(
        token: String,
        serverLoadId: Long,
    ): Result<Unit>

}
