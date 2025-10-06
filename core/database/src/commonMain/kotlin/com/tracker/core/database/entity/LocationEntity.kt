package com.tracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для хранения GPS координат в локальной базе данных
 */
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double?,
    val speed: Float?,
    val bearing: Float?,
    val timestamp: Long, // Храним как Long (milliseconds since epoch)
    
    // Метаданные
    val batteryLevel: Float?,
    val isSent: Boolean = false, // Флаг для отслеживания отправки на сервер
    val createdAt: Long // Время создания записи
)
