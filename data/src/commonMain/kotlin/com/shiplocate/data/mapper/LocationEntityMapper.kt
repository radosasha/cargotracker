package com.shiplocate.data.mapper

import com.shiplocate.core.database.entity.LocationEntity
import com.shiplocate.domain.model.DeviceLocation
import com.shiplocate.domain.model.GpsLocation
import kotlinx.datetime.Instant

/**
 * Маппер для конвертации между LocationEntity и Domain Location
 */
object LocationEntityMapper {
    /**
     * Конвертирует Domain Location в LocationEntity для сохранения в БД
     */
    fun toEntity(
        location: GpsLocation,
        batteryLevel: Float? = null,
    ): LocationEntity {
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
            createdAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
        )
    }

    /**
     * Конвертирует LocationEntity из БД в Domain Location
     */
    fun toDomain(
        entity: LocationEntity,
    ): GpsLocation {
        return GpsLocation(
            latitude = entity.latitude,
            longitude = entity.longitude,
            accuracy = entity.accuracy,
            altitude = entity.altitude,
            speed = entity.speed,
            bearing = entity.bearing,
            timestamp = Instant.fromEpochMilliseconds(entity.timestamp),
        )
    }

    fun toDomainDeviceLocation(
        entity: LocationEntity,
    ): DeviceLocation {
        return DeviceLocation(
            latitude = entity.latitude,
            longitude = entity.longitude,
            accuracy = entity.accuracy,
            altitude = entity.altitude,
            speed = entity.speed,
            bearing = entity.bearing,
            timestamp = Instant.fromEpochMilliseconds(entity.timestamp),
            batteryLevel = entity.batteryLevel
        )
    }
}
