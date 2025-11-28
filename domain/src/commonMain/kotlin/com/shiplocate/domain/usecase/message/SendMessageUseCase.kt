package com.shiplocate.domain.usecase.message

import com.shiplocate.domain.model.message.Message
import com.shiplocate.domain.repository.AuthPreferencesRepository
import com.shiplocate.domain.repository.MessagesRepository

/**
 * Use case to send a message
 * Sends message to server and updates local database
 */
class SendMessageUseCase(
    private val messagesRepository: MessagesRepository,
    private val authPreferencesRepository: AuthPreferencesRepository,
) {
    /**
     * Send a message
     * Automatically retrieves auth token from preferences
     * @param loadId Load ID (local)
     * @param messageText Message text
     * @return Result with created Message
     */
    suspend operator fun invoke(
        loadId: Long,
        message: Message,
    ): Result<Message> {
        // Get auth token
        val authSession = authPreferencesRepository.getSession()
        val token = authSession?.token

        if (token == null) {
            return Result.failure(Exception("Not authenticated"))
        }

        return messagesRepository.sendMessage(token, loadId, message)
    }
}

