package com.shiplocate.data.network.api

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.core.network.bodyOrThrow
import com.shiplocate.data.network.dto.load.ConnectLoadRequest
import com.shiplocate.data.network.dto.load.DisconnectLoadRequest
import com.shiplocate.data.network.dto.load.EnterStopRequest
import com.shiplocate.data.network.dto.load.LoadDto
import com.shiplocate.data.network.dto.load.PingLoadRequest
import com.shiplocate.data.network.dto.load.RouteCoordinateDto
import com.shiplocate.data.network.dto.load.RouteDto
import com.shiplocate.data.network.dto.load.RouteRequestDto
import com.shiplocate.data.network.dto.load.StopDto
import com.shiplocate.data.network.dto.load.UpdateStopCompletionRequest
import com.shiplocate.data.network.dto.message.MessageDto
import com.shiplocate.data.network.dto.message.SendMessageRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
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
     * @param serverLoadId Load ID to connect to
     * @return Updated list of LoadDto
     * @throws Exception if request fails
     */
    suspend fun connectToLoad(
        token: String,
        serverLoadId: Long,
    ): List<LoadDto>

    /**
     * Disconnect from load
     * Sets loadstatus=2 for the specified load
     *
     * @param token JWT token for authentication
     * @param serverLoadId Load ID to disconnect from
     * @return Updated list of LoadDto
     * @throws Exception if request fails
     */
    suspend fun disconnectFromLoad(
        token: String,
        serverLoadId: Long,
    ): List<LoadDto>

    /**
     * Ping load to update connection status
     * Updates connectionStatus to "online" and lastUpdate timestamp
     *
     * @param token JWT token for authentication
     * @param loadId Load ID to ping
     * @throws Exception if request fails
     */
    suspend fun pingLoad(
        token: String,
        loadId: Long,
    )

    /**
     * Enter stop - notify server that driver entered a stop
     * Sets enter=true for the specified stop
     *
     * @param token JWT token for authentication
     * @param stopId Stop ID to enter
     * @return true if successful (200 OK or 400 Bad Request), false otherwise
     */
    suspend fun enterStop(
        token: String,
        stopId: Long,
    ): Boolean

    /**
     * Reject load
     * Rejects the specified load
     *
     * @param token JWT token for authentication
     * @param serverLoadId Load ID to reject
     * @return Updated list of LoadDto
     * @throws Exception if request fails
     */
    suspend fun rejectLoad(
        token: String,
        serverLoadId: Long,
    ): List<LoadDto>

    /**
     * Update stop completion status
     * Updates the completion field for a stop
     *
     * @param token JWT token for authentication
     * @param stopId Stop ID to update
     * @param completion Completion status (0 = NOT_COMPLETED, 1 = COMPLETED)
     * @return Updated StopDto
     * @throws Exception if request fails
     */
    suspend fun updateStopCompletion(
        token: String,
        stopId: Long,
        completion: Int,
    ): StopDto

    /**
     * Get messages for a load
     *
     * @param token JWT token for authentication
     * @param loadId Load ID to get messages for
     * @return List of MessageDto
     * @throws Exception if request fails
     */
    suspend fun getMessages(
        token: String,
        loadId: Long,
    ): List<MessageDto>

    /**
     * Send a message for a load
     *
     * @param token JWT token for authentication
     * @param loadId Load ID
     * @param message Message text
     * @return Created MessageDto
     * @throws Exception if request fails
     */
    suspend fun sendMessage(
        token: String,
        loadId: Long,
        message: String,
    ): MessageDto

    /**
     * Get route for a load
     * Computes Google Routes legs response for the stops associated with a load
     *
     * @param token JWT token for authentication
     * @param loadId Load ID to get route for
     * @param startLat Start latitude coordinate
     * @param startLon Start longitude coordinate
     * @return RouteDto
     * @throws Exception if request fails
     */
    suspend fun getRoute(
        token: String,
        loadId: Long,
        startLat: Double,
        startLon: Double,
    ): RouteDto
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
        serverLoadId: Long,
    ): List<LoadDto> {
        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Connecting to load $serverLoadId")

        val request = ConnectLoadRequest(serverLoadId = serverLoadId)
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
        serverLoadId: Long,
    ): List<LoadDto> {
        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Disconnecting from load $serverLoadId")

        val request = DisconnectLoadRequest(serverLoadId = serverLoadId)
        val response =
            httpClient.post("$baseUrl/api/mobile/loads/disconnect") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Disconnect response status: ${response.status}")
        return response.bodyOrThrow()
    }

    override suspend fun pingLoad(
        token: String,
        loadId: Long,
    ) {
        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Pinging load $loadId")

        val request = PingLoadRequest(loadId = loadId)
        val response =
            httpClient.post("$baseUrl/api/mobile/loads/ping") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Ping response status: ${response.status}")
        // Server returns {status: "ok", message: "Connection status updated"}
        // bodyOrThrow will throw exception if status is not success
        // We read body as String to verify request succeeded, but don't need to parse it
        response.bodyOrThrow<String>() // This verifies status is success
    }

    override suspend fun enterStop(
        token: String,
        stopId: Long,
    ): Boolean {
        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Entering stop $stopId")

        val request = EnterStopRequest(stopId = stopId)
        val response =
            httpClient.post("$baseUrl/api/mobile/loads/enterstop") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Enter stop response status: ${response.status}")

        // Consider 200 OK or 404 Not found as success (404 means not found, but operation is processed)
        val statusCode = response.status.value
        return statusCode in 200..299 || statusCode == 404
    }

    override suspend fun rejectLoad(
        token: String,
        serverLoadId: Long,
    ): List<LoadDto> {
        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Rejecting load $serverLoadId")

        val request = ConnectLoadRequest(serverLoadId = serverLoadId)
        val response =
            httpClient.post("$baseUrl/api/mobile/loads/reject") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Reject response status: ${response.status}")

        // Обрабатываем 200 OK или 400 Bad Request как успех
        val statusCode = response.status.value
        if (statusCode == HttpStatusCode.OK.value || statusCode == HttpStatusCode.BadRequest.value) {
            return try {
                response.bodyOrThrow()
            } catch (e: ClientRequestException) {
                // Если 400, возвращаем пустой список (операция выполнена)
                if (statusCode == HttpStatusCode.BadRequest.value) {
                    logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Reject returned 400, treating as success")
                    emptyList()
                } else {
                    throw e
                }
            }
        } else {
            return response.bodyOrThrow()
        }
    }

    override suspend fun updateStopCompletion(
        token: String,
        stopId: Long,
        completion: Int,
    ): StopDto {
        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Updating stop completion for stop $stopId to $completion")

        val request = UpdateStopCompletionRequest(stopId = stopId, completion = completion)

        return try {
            val response = httpClient.post("$baseUrl/api/mobile/loads/stopcompletion") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Update stop completion response status: ${response.status}")
            response.bodyOrThrow<StopDto>()
        } catch (e: ResponseException) {
            logger.debug(LogCategory.NETWORK, "🌐 LoadApi: ❌ Update stop completion error: ${e.response.status}")
            val errorMessage = try {
                e.response.body<String>()
            } catch (parseError: Exception) {
                logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Failed to parse error response: ${parseError.message}")
                "Stop ID and completion are required"
            }
            throw Exception(errorMessage)
        } catch (e: Exception) {
            logger.debug(LogCategory.NETWORK, "🌐 LoadApi: ❌ Network error: ${e.message}")
            throw e
        }
    }

    override suspend fun getMessages(
        token: String,
        loadId: Long,
    ): List<MessageDto> {
        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Getting messages for load $loadId")

        return try {
            val response = httpClient.get("$baseUrl/api/mobile/loads/messages") {
                header(HttpHeaders.Authorization, "Bearer $token")
                url {
                    parameters.append("loadId", loadId.toString())
                }
            }

            logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Get messages response status: ${response.status}")
            response.bodyOrThrow<List<MessageDto>>()
        } catch (e: ResponseException) {
            logger.debug(LogCategory.NETWORK, "🌐 LoadApi: ❌ Get messages error: ${e.response.status}")
            val errorMessage = try {
                e.response.body<String>()
            } catch (parseError: Exception) {
                logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Failed to parse error response: ${parseError.message}")
                "Failed to get messages"
            }
            throw Exception(errorMessage)
        } catch (e: Exception) {
            logger.debug(LogCategory.NETWORK, "🌐 LoadApi: ❌ Network error: ${e.message}")
            throw e
        }
    }

    override suspend fun sendMessage(
        token: String,
        loadId: Long,
        message: String,
    ): MessageDto {
        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Sending message for load $loadId")

        val request = SendMessageRequest(loadId = loadId, message = message.trim())

        return try {
            val response = httpClient.post("$baseUrl/api/mobile/loads/message") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Send message response status: ${response.status}")
            response.bodyOrThrow<MessageDto>()
        } catch (e: ResponseException) {
            logger.debug(LogCategory.NETWORK, "🌐 LoadApi: ❌ Send message error: ${e.response.status}")
            val errorMessage = try {
                e.response.body<String>()
            } catch (parseError: Exception) {
                logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Failed to parse error response: ${parseError.message}")
                "Failed to send message"
            }
            throw Exception(errorMessage)
        } catch (e: Exception) {
            logger.debug(LogCategory.NETWORK, "🌐 LoadApi: ❌ Network error: ${e.message}")
            throw e
        }
    }

    override suspend fun getRoute(
        token: String,
        loadId: Long,
        startLat: Double,
        startLon: Double,
    ): RouteDto {
        logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Getting route for load $loadId with start coordinates ($startLat, $startLon)")

        return try {
            val routeRequest = RouteRequestDto(
                loadId = loadId,
                start = RouteCoordinateDto(
                    lat = startLat,
                    lon = startLon,
                ),
            )

            val response = httpClient.post("$baseUrl/api/mobile/loads/route") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, "application/json")
                setBody(routeRequest)
            }

            logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Get route response status: ${response.status}")
            response.bodyOrThrow<RouteDto>()
        } catch (e: ResponseException) {
            logger.debug(LogCategory.NETWORK, "🌐 LoadApi: ❌ Get route error: ${e.response.status}")
            val errorMessage = try {
                e.response.body<String>()
            } catch (parseError: Exception) {
                logger.debug(LogCategory.NETWORK, "🌐 LoadApi: Failed to parse error response: ${parseError.message}")
                "Failed to get route"
            }
            throw Exception(errorMessage)
        } catch (e: Exception) {
            logger.debug(LogCategory.NETWORK, "🌐 LoadApi: ❌ Network error: ${e.message}")
            throw e
        }
    }
}
