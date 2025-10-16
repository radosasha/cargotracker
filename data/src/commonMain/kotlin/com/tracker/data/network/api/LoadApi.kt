package com.tracker.data.network.api

import com.tracker.core.network.bodyOrThrow
import com.tracker.data.network.dto.load.LoadDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders

/**
 * API interface for Load operations
 */
interface LoadApi {
    /**
     * Get loads for authenticated user
     * Requires Bearer token in Authorization header
     *
     * @param token JWT token for authentication
     * @return List of LoadDto
     * @throws Exception if request fails
     */
    suspend fun getLoads(token: String): List<LoadDto>

    /**
     * Connect to load
     * Sets loadstatus=1 for the specified load and loadstatus=2 for all other loads with loadstatus=1
     *
     * @param token JWT token for authentication
     * @param loadId Load ID to connect to
     * @return Updated list of LoadDto
     * @throws Exception if request fails
     */
    suspend fun connectToLoad(
        token: String,
        loadId: String,
    ): List<LoadDto>

    /**
     * Disconnect from load
     * Sets loadstatus=2 for the specified load
     *
     * @param token JWT token for authentication
     * @param loadId Load ID to disconnect from
     * @return Updated list of LoadDto
     * @throws Exception if request fails
     */
    suspend fun disconnectFromLoad(
        token: String,
        loadId: String,
    ): List<LoadDto>
}

/**
 * Implementation of LoadApi
 * Calls /api/mobile/loads endpoint
 */
class LoadApiImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) : LoadApi {
    override suspend fun getLoads(token: String): List<LoadDto> {
        println("🌐 LoadApi: Getting loads from $baseUrl")

        val response =
            httpClient.get("$baseUrl/api/mobile/loads") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

        println("🌐 LoadApi: Response status: ${response.status}")
        return response.bodyOrThrow()
    }

    override suspend fun connectToLoad(
        token: String,
        loadId: String,
    ): List<LoadDto> {
        println("🌐 LoadApi: Connecting to load $loadId")

        val response =
            httpClient.post("$baseUrl/api/mobile/loads/$loadId/connect") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

        println("🌐 LoadApi: Connect response status: ${response.status}")
        return response.bodyOrThrow()
    }

    override suspend fun disconnectFromLoad(
        token: String,
        loadId: String,
    ): List<LoadDto> {
        println("🌐 LoadApi: Disconnecting from load $loadId")

        val response =
            httpClient.post("$baseUrl/api/mobile/loads/$loadId/disconnect") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

        println("🌐 LoadApi: Disconnect response status: ${response.status}")
        return response.bodyOrThrow()
    }
}
