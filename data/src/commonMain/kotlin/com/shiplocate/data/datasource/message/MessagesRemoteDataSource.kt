package com.shiplocate.data.datasource.message

import com.shiplocate.data.network.api.LoadApi
import com.shiplocate.data.network.dto.message.MessageDto

/**
 * Remote data source for Messages operations
 * Handles API calls through LoadApi
 */
class MessagesRemoteDataSource(
    private val loadApi: LoadApi,
) {
    /**
     * Get messages for a load
     * @param token Bearer token for authentication
     * @param loadId Load ID to get messages for
     * @return List of MessageDto
     */
    suspend fun getMessages(
        token: String,
        loadId: Long,
    ): List<MessageDto> {
        println("📡 MessagesRemoteDataSource: Getting messages for load $loadId")
        return loadApi.getMessages(token, loadId)
    }

    /**
     * Send a message for a load
     * @param token Bearer token for authentication
     * @param loadId Load ID
     * @param message Message text
     * @return Created MessageDto
     */
    suspend fun sendMessage(
        token: String,
        loadId: Long,
        message: String,
    ): MessageDto {
        println("📡 MessagesRemoteDataSource: Sending message for load $loadId")
        return loadApi.sendMessage(token, loadId, message)
    }
}

