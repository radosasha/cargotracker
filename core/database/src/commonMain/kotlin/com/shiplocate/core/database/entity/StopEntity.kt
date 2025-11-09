package com.shiplocate.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Room entity for Stop (Drop) caching
 */
@Entity(
    tableName = "stops",
    foreignKeys = [
        ForeignKey(
            entity = LoadEntity::class,
            parentColumns = ["serverId"],
            childColumns = ["loadServerId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class StopEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "loadServerId", index = true)
    val loadServerId: Long, // Foreign key to LoadEntity.serverId
    val serverId: Long, // Server's stop ID
    val type: Int,
    val locationAddress: String,
    val date: Long,
    val geofenceRadius: Int,
    val stopIndex: Int,
)

