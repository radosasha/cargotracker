package com.shiplocate.data.network.api

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.core.network.bodyOrThrow
import com.shiplocate.data.network.dto.load.ConnectLoadRequest
import com.shiplocate.data.network.dto.load.DisconnectLoadRequest
import com.shiplocate.data.network.dto.load.LoadDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

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
    private val logger: Logger,
) : LoadApi {
    override suspend fun getLoads(token: String): List<LoadDto> {
        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Getting loads from $baseUrl")

        val response =
            httpClient.get("$baseUrl/api/mobile/loads") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Response status: ${response.status}")
        return response.bodyOrThrow()
    }

    override suspend fun connectToLoad(
        token: String,
        loadId: String,
    ): List<LoadDto> {
        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Connecting to load $loadId")

        val request = ConnectLoadRequest(loadId = loadId)
        val response =
            httpClient.post("$baseUrl/api/mobile/loads/connect") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Connect response status: ${response.status}")
        return response.bodyOrThrow()
    }

    override suspend fun disconnectFromLoad(
        token: String,
        loadId: String,
    ): List<LoadDto> {
        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Disconnecting from load $loadId")

        val request = DisconnectLoadRequest(loadId = loadId)
        val response =
            httpClient.post("$baseUrl/api/mobile/loads/disconnect") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Disconnect response status: ${response.status}")
        return response.bodyOrThrow()
    }
}
