package com.shiplocate.domain.usecase.message

import com.shiplocate.domain.model.message.Message
import com.shiplocate.domain.repository.AuthRepository
import com.shiplocate.domain.repository.MessagesRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get messages as Flow from local database
 * Messages are automatically updated when database changes
 */
class ObserveMessagesUseCase(
    private val messagesRepository: MessagesRepository,
    private val authRepository: AuthRepository,
) {
    /**
     * Get messages for a load as Flow
     * Automatically retrieves auth token from preferences
     * @param loadId Load ID
     * @return Flow of list of Messages, or null if not authenticated
     */
    suspend operator fun invoke(loadId: Long): Flow<List<Message>>? {
        // Get auth token
        val authSession = authRepository.getSession()
        val token = authSession?.token

        if (token == null) {
            return null
        }

        // getMessages is not suspend, it returns Flow directly
        return messagesRepository.getMessages(token, loadId)
    }
}

