package com.tracker.data.mapper

import com.tracker.core.database.entity.LocationEntity
import com.tracker.domain.model.Location
import kotlinx.datetime.Instant

/**
 * Маппер для конвертации между LocationEntity и Domain Location
 */
object LocationEntityMapper {
    
    /**
     * Конвертирует Domain Location в LocationEntity для сохранения в БД
     */
    fun toEntity(location: Location, batteryLevel: Float? = null): LocationEntity {
        return LocationEntity(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            altitude = location.altitude,
            speed = location.speed,
            bearing = location.bearing,
            timestamp = location.timestamp.toEpochMilliseconds(),
            batteryLevel = batteryLevel,
            isSent = false,
            createdAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        )
    }
    
    /**
     * Конвертирует LocationEntity из БД в Domain Location
     */
    fun toDomain(entity: LocationEntity, deviceId: String): Location {
        return Location(
            latitude = entity.latitude,
            longitude = entity.longitude,
            accuracy = entity.accuracy,
            altitude = entity.altitude,
            speed = entity.speed,
            bearing = entity.bearing,
            timestamp = Instant.fromEpochMilliseconds(entity.timestamp),
            deviceId = deviceId
        )
    }
    
    /**
     * Конвертирует список LocationEntity в список Domain Location
     */
    fun toDomainList(entities: List<LocationEntity>, deviceId: String): List<Location> {
        return entities.map { toDomain(it, deviceId) }
    }
}

