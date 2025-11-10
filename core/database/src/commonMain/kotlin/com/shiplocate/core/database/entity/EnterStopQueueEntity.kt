package com.shiplocate.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for EnterStopQueue
 * Stores stop IDs that need to be sent to server
 */
@Entity(tableName = "enter_stop_queue")
data class EnterStopQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "stopId", index = true)
    val stopId: Long, // Server's stop ID
)

