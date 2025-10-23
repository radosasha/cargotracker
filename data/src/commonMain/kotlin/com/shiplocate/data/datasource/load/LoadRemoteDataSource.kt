package com.shiplocate.data.datasource.load

import com.shiplocate.data.network.api.LoadApi
import com.shiplocate.data.network.dto.load.LoadDto

/**
 * Remote data source for Load operations
 * Handles API calls through LoadApi
 */
class LoadRemoteDataSource(
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
     * @param loadId Load ID to connect to
     * @return Updated list of LoadDto
     */
    suspend fun connectToLoad(
        token: String,
        loadId: String,
    ): List<LoadDto> {
        println("📡 LoadRemoteDataSource: Connecting to load $loadId")
        return loadApi.connectToLoad(token, loadId)
    }

    /**
     * Disconnect from load
     * @param token Bearer token for authentication
     * @param loadId Load ID to disconnect from
     * @return Updated list of LoadDto
     */
    suspend fun disconnectFromLoad(
        token: String,
        loadId: String,
    ): List<LoadDto> {
        println("📡 LoadRemoteDataSource: Disconnecting from load $loadId")
        return loadApi.disconnectFromLoad(token, loadId)
    }
}
