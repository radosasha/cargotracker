package com.tracker.data.mapper

import com.tracker.data.model.GpsLocation
import com.tracker.domain.model.Location

/**
 * Маппер для преобразования между GpsLocation и Domain Location
 */
object GpsLocationMapper {
    /**
     * Преобразует GpsLocation в Domain Location
     */
    fun toDomain(
        gpsLocation: GpsLocation,
        deviceId: String,
    ): Location {
        return Location(
            latitude = gpsLocation.latitude,
            longitude = gpsLocation.longitude,
            accuracy = gpsLocation.accuracy,
            altitude = gpsLocation.altitude,
            speed = gpsLocation.speed,
            bearing = gpsLocation.bearing,
            timestamp = gpsLocation.timestamp,
            deviceId = deviceId,
        )
    }

    /**
     * Преобразует список GpsLocation в список Domain Location
     */
    fun toDomainList(
        gpsLocations: List<GpsLocation>,
        deviceId: String,
    ): List<Location> {
        return gpsLocations.map { toDomain(it, deviceId) }
    }
}
