package com.shiplocate.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shiplocate.core.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Message database operations
 */
@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE loadId = :loadId ORDER BY datetime ASC")
    fun getMessagesByLoadId(loadId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE loadId = :loadId ORDER BY datetime ASC")
    suspend fun getMessagesByLoadIdSync(loadId: Long): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE serverId = :serverId LIMIT 1")
    suspend fun getMessageByServerId(serverId: Long): MessageEntity?

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Long): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE loadId = :loadId")
    suspend fun deleteMessagesByLoadId(loadId: Long)
}

