package com.shiplocate.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for Message caching
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = LoadEntity::class,
            parentColumns = ["id"],
            childColumns = ["loadId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["loadId"]),
        Index(value = ["serverId"], unique = true),
    ],
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "loadId")
    val loadId: Long, // Foreign key to LoadEntity.id
    val serverId: Long, // Server's message ID (0 for unsent messages)
    val message: String,
    val type: Int, // 0 = DISPATCHER, 1 = DRIVER
    val datetime: Long, // Unix timestamp in milliseconds
)

