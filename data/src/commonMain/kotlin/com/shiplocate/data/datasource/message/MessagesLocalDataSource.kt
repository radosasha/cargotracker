package com.shiplocate.data.datasource.message

import com.shiplocate.core.database.TrackerDatabase
import com.shiplocate.core.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Local data source for Message caching
 * Handles database operations through Room
 */
class MessagesLocalDataSource(
    private val database: TrackerDatabase,
) {
    private val messageDao = database.messageDao()

    /**
     * Get messages for a load as Flow
     */
    fun getMessagesByLoadId(loadId: Long): Flow<List<MessageEntity>> {
        println("💾 MessagesLocalDataSource: Getting messages by loadId=$loadId")
        return messageDao.getMessagesByLoadIdFlow(loadId)
    }

    /**
     * Get messages for a load synchronously
     */
    suspend fun getMessagesByLoadIdSync(loadId: Long): List<MessageEntity> {
        println("💾 MessagesLocalDataSource: Getting messages by loadId=$loadId (sync)")
        return messageDao.getMessagesByLoadId(loadId)
    }

    /**
     * Get a single message by its server ID
     */
    suspend fun getMessageByServerId(serverId: Long): MessageEntity? {
        println("💾 MessagesLocalDataSource: Getting message by serverId=$serverId")
        return messageDao.getMessageByServerId(serverId)
    }

    /**
     * Insert a single message
     */
    suspend fun insertMessage(message: MessageEntity): Long {
        println("💾 MessagesLocalDataSource: Inserting message with serverId=${message.serverId}")
        return messageDao.insertMessage(message)
    }

    /**
     * Insert multiple messages
     */
    suspend fun insertMessages(messages: List<MessageEntity>) {
        println("💾 MessagesLocalDataSource: Inserting ${messages.size} messages")
        messageDao.insertMessages(messages)
    }

    /**
     * Update a message
     */
    suspend fun updateMessage(message: MessageEntity) {
        println("💾 MessagesLocalDataSource: Updating message with serverId=${message.serverId}")
        messageDao.updateMessage(message)
    }

    /**
     * Update multiple messages
     */
    suspend fun updateMessages(messages: List<MessageEntity>) {
        println("💾 MessagesLocalDataSource: Updating ${messages.size} messages")
        messageDao.updateMessages(messages)
    }
}

