package com.shiplocate.domain.repository

import com.shiplocate.domain.model.message.Message
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Messages data operations
 * Handles both remote API calls and local caching
 */
interface MessagesRepository {
    /**
     * Get messages for a load as Flow
     * @param token Authentication token
     * @param loadId Load ID
     * @return Flow of list of Messages
     */
    fun getMessages(
        token: String,
        loadId: Long,
    ): Flow<List<Message>>

    /**
     * Refresh messages from server
     * @param token Authentication token
     * @param loadId Load ID
     * @return Result with list of Messages
     */
    suspend fun refreshMessages(
        token: String,
        loadId: Long,
    ): Result<List<Message>>

    /**
     * Send a message
     * @param token Authentication token
     * @param loadId Load ID (local)
     * @param messageText Message text
     * @return Result with created Message
     */
    suspend fun sendMessage(
        token: String,
        loadId: Long,
        message: Message,
    ): Result<Message>

    /**
     * Update message after successful send
     * @param message Updated message from server
     * @return Result with updated Message
     */
    suspend fun updateMessage(message: Message): Result<Message>
}

