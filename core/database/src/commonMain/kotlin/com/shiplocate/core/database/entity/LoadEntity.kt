package com.shiplocate.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for Load caching
 */
@Entity(tableName = "loads")
data class LoadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val loadName: String,
    val serverId: Long,
    val description: String?,
    val lastUpdated: Long?,
    val createdAt: Long,
    val loadStatus: Int,
)
