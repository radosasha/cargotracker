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
            parentColumns = ["id"],
            childColumns = ["loadId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class StopEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "loadId", index = true)
    val loadId: Long, // Foreign key to LoadEntity.id
    val serverId: Long, // Server's stop ID
    val type: Int,
    val locationName: String,
    val locationAddress: String,
    val date: Long,
    val geofenceRadius: Int,
    val stopIndex: Int,
    val latitude: Double,
    val longitude: Double,
    val enter: Boolean,
    val note: String? = null,
    @ColumnInfo(defaultValue = "0")
    val completion: Int = 0, // 0 = NOT_COMPLETED, 1 = COMPLETED
)

