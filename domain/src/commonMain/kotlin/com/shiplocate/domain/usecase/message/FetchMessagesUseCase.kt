package com.shiplocate.domain.usecase.message

import com.shiplocate.domain.model.message.Message
import com.shiplocate.domain.repository.AuthRepository
import com.shiplocate.domain.repository.MessagesRepository

/**
 * Use case to refresh messages from server
 * Fetches messages from server and caches them locally
 */
class FetchMessagesUseCase(
    private val messagesRepository: MessagesRepository,
    private val authRepository: AuthRepository,
) {
    /**
     * Refresh messages from server
     * Automatically retrieves auth token from preferences
     * @param loadId Load ID
     * @return Result with list of Messages
     */
    suspend operator fun invoke(loadId: Long): Result<List<Message>> {
        // Get auth token
        val authSession = authRepository.getSession()
        val token = authSession?.token

        if (token == null) {
            return Result.failure(Exception("Not authenticated"))
        }

        return messagesRepository.refreshMessages(token, loadId)
    }
}

