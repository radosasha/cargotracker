package com.shiplocate.data.repository

import com.shiplocate.core.database.entity.MessageEntity
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.load.LoadsLocalDataSource
import com.shiplocate.data.datasource.message.MessagesLocalDataSource
import com.shiplocate.data.datasource.message.MessagesRemoteDataSource
import com.shiplocate.data.mapper.toMessageDomain
import com.shiplocate.data.mapper.toMessageEntity
import com.shiplocate.domain.model.message.Message
import com.shiplocate.domain.repository.MessagesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of MessagesRepository
 * Handles fetching messages from server with automatic caching fallback
 */
class MessagesRepositoryImpl(
    private val messagesRemoteDataSource: MessagesRemoteDataSource,
    private val messagesLocalDataSource: MessagesLocalDataSource,
    private val loadsLocalDataSource: LoadsLocalDataSource,
    private val logger: Logger,
) : MessagesRepository {
    override fun getMessages(
        token: String,
        loadId: Long,
    ): Flow<List<Message>> {
        logger.info(LogCategory.GENERAL, "🔄 MessagesRepositoryImpl: Getting messages for load $loadId")
        return messagesLocalDataSource.getMessagesByLoadId(loadId)
            .map { entities -> entities.map { it.toMessageDomain() } }
    }

    override suspend fun refreshMessages(
        token: String,
        loadId: Long,
    ): Result<List<Message>> {
        logger.info(LogCategory.GENERAL, "🔄 MessagesRepositoryImpl: Refreshing messages for load $loadId")

        return try {
            // Get serverLoadId from local loadId
            val loadEntity = loadsLocalDataSource.getLoadById(loadId)
            val serverLoadId = loadEntity?.serverId ?: loadId

            val messageDtos = messagesRemoteDataSource.getMessages(token, serverLoadId)

            // Get existing messages from database
            val existingMessages = messagesLocalDataSource.getMessagesByLoadIdSync(loadId)

            // Separate messages into new and existing
            val messagesToInsert = mutableListOf<MessageEntity>()
            val messagesToUpdate = mutableListOf<MessageEntity>()

            messageDtos.forEach { messageDto ->
                val existingMessage = existingMessages.find { it.serverId == messageDto.id }
                if (existingMessage != null) {
                    // Update existing message with new data, keeping the same local id
                    val updatedEntity = messageDto.toMessageEntity(loadId).copy(id = existingMessage.id)
                    messagesToUpdate.add(updatedEntity)
                } else {
                    // New message: set id = 0 so Room will auto-generate a new id
                    messagesToInsert.add(messageDto.toMessageEntity(loadId).copy(id = 0))
                }
            }

            // Insert new messages
            if (messagesToInsert.isNotEmpty()) {
                logger.info(LogCategory.GENERAL, "💾 MessagesRepositoryImpl: Inserting ${messagesToInsert.size} new messages")
                messagesLocalDataSource.insertMessages(messagesToInsert)
            }

            // Update existing messages
            if (messagesToUpdate.isNotEmpty()) {
                logger.info(LogCategory.GENERAL, "💾 MessagesRepositoryImpl: Updating ${messagesToUpdate.size} existing messages")
                messagesLocalDataSource.updateMessages(messagesToUpdate)
            }

            logger.info(LogCategory.GENERAL, "✅ MessagesRepositoryImpl: Successfully refreshed ${messageDtos.size} messages")
            Result.success(messageDtos.map { it.toMessageDomain() })
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "❌ MessagesRepositoryImpl: Failed to refresh messages: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(
        token: String,
        loadId: Long,
        message: Message,
    ): Result<Message> {
        logger.info(LogCategory.GENERAL, "🔄 MessagesRepositoryImpl: Sending message for load $loadId")

        return try {
            // Get serverLoadId from local loadId
            val loadEntity = loadsLocalDataSource.getLoadById(loadId)
            val serverLoadId = loadEntity?.serverId ?: loadId

            val localMessageId = saveLocalMessage(message)

            val messageDto = messagesRemoteDataSource.sendMessage(token, serverLoadId, message.message)

            // Convert to entity and save to database (this will replace the temporary message)
            val entity = messageDto.toMessageEntity(loadId).copy(id = localMessageId) // Using local loadId for database
            messagesLocalDataSource.updateMessage(entity)

            logger.info(LogCategory.GENERAL, "✅ MessagesRepositoryImpl: Successfully sent message")
            Result.success(messageDto.toMessageDomain())
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "❌ MessagesRepositoryImpl: Failed to send message: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateMessage(message: Message): Result<Message> {
        logger.info(LogCategory.GENERAL, "🔄 MessagesRepositoryImpl: Updating message ${message.id}")

        return try {
            // Find existing message by serverId
            val existingEntity = messagesLocalDataSource.getMessageByServerId(message.id)

            if (existingEntity != null) {
                val updatedEntity = message.toMessageEntity(existingEntity.loadId)
                messagesLocalDataSource.updateMessage(updatedEntity)
                logger.info(LogCategory.GENERAL, "✅ MessagesRepositoryImpl: Successfully updated message")
                Result.success(message)
            } else {
                logger.warn(LogCategory.GENERAL, "⚠️ MessagesRepositoryImpl: Message not found in local database")
                Result.success(message)
            }
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "❌ MessagesRepositoryImpl: Failed to update message: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun saveLocalMessage(message: Message): Long {
        logger.info(LogCategory.GENERAL, "🔄 MessagesRepositoryImpl: Saving temporary message")


        val entity = message.toMessageEntity(message.loadId)
        return messagesLocalDataSource.insertMessage(entity)
    }
}

