package com.shiplocate.data.datasource.load

import com.shiplocate.data.network.api.LoadApi
import com.shiplocate.data.network.dto.load.LoadDto
import com.shiplocate.data.network.dto.load.RouteDto
import com.shiplocate.data.network.dto.load.StopDto

/**
 * Remote data source for Load operations
 * Handles API calls through LoadApi
 */
class LoadsRemoteDataSource(
    private val loadApi: LoadApi,
) {
    /**
     * Fetch loads from server
     * @param token Bearer token for authentication
     * @return List of LoadDto
     */
    suspend fun getLoads(token: String): List<LoadDto> {
        println("📡 LoadRemoteDataSource: Fetching loads from server")
        return loadApi.getLoads(token)
    }

    /**
     * Connect to load
     * @param token Bearer token for authentication
     * @param serverLoadId Load ID to connect to
     * @return Updated list of LoadDto
     */
    suspend fun connectToLoad(
        token: String,
        serverLoadId: Long,
    ): List<LoadDto> {
        println("📡 LoadRemoteDataSource: Connecting to load $serverLoadId")
        return loadApi.connectToLoad(token, serverLoadId)
    }

    /**
     * Disconnect from load
     * @param token Bearer token for authentication
     * @param serverLoadId Load ID to disconnect from
     * @return Updated list of LoadDto
     */
    suspend fun disconnectFromLoad(
        token: String,
        serverLoadId: Long,
    ): List<LoadDto> {
        println("📡 LoadRemoteDataSource: Disconnecting from load $serverLoadId")
        return loadApi.disconnectFromLoad(token, serverLoadId)
    }

    /**
     * Ping load to update connection status
     * @param token Bearer token for authentication
     * @param loadId Load ID to ping
     */
    suspend fun pingLoad(
        token: String,
        loadId: Long,
    ) {
        println("📡 LoadRemoteDataSource: Pinging load $loadId")
        loadApi.pingLoad(token, loadId)
    }

    /**
     * Enter stop - notify server that driver entered a stop
     * @param token Bearer token for authentication
     * @param stopId Stop ID to enter
     * @return true if successful (200 OK or 400 Bad Request), false otherwise
     */
    suspend fun enterStop(
        token: String,
        stopId: Long,
    ): Boolean {
        println("📡 LoadRemoteDataSource: Entering stop $stopId")
        return loadApi.enterStop(token, stopId)
    }

    /**
     * Reject load
     * @param token Bearer token for authentication
     * @param serverLoadId Load ID to reject
     * @return Updated list of LoadDto
     */
    suspend fun rejectLoad(
        token: String,
        serverLoadId: Long,
    ): List<LoadDto> {
        println("📡 LoadRemoteDataSource: Rejecting load $serverLoadId")
        return loadApi.rejectLoad(token, serverLoadId)
    }

    /**
     * Update stop completion status
     * @param token Bearer token for authentication
     * @param stopId Server's stop ID
     * @param completion Completion status (0 = NOT_COMPLETED, 1 = COMPLETED)
     * @return Updated StopDto
     */
    suspend fun updateStopCompletion(
        token: String,
        stopId: Long,
        completion: Int,
    ): StopDto {
        println("📡 LoadRemoteDataSource: Updating stop completion for stop $stopId to $completion")
        return loadApi.updateStopCompletion(token, stopId, completion)
    }

    /**
     * Get route for a load
     * @param token Bearer token for authentication
     * @param loadId Load ID to get route for
     * @return RouteDto
     */
    suspend fun getRoute(
        token: String,
        loadId: Long,
    ): RouteDto {
        println("📡 LoadRemoteDataSource: Getting route for load $loadId")
        return loadApi.getRoute(token, loadId)
    }
}
