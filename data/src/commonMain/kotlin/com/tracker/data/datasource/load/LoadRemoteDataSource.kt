package com.tracker.data.datasource.load

import com.tracker.data.network.api.LoadApi
import com.tracker.data.network.dto.load.LoadDto

/**
 * Remote data source for Load operations
 * Handles API calls through LoadApi
 */
class LoadRemoteDataSource(
    private val loadApi: LoadApi
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
}



