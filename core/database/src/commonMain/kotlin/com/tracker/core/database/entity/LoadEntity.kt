package com.tracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for Load caching
 */
@Entity(tableName = "loads")
data class LoadEntity(
    @PrimaryKey
    val loadId: String,
    val description: String?,
    val lastUpdated: Long?,
    val createdAt: Long?
)


