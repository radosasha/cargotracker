package com.shiplocate.data.mapper

import com.shiplocate.data.model.GpsLocation
import com.shiplocate.domain.model.Location

/**
 * Маппер для преобразования между GpsLocation и Domain Location
 */
object GpsLocationMapper {
    /**
     * Преобразует GpsLocation в Domain Location
     */
    fun toDomain(
        gpsLocation: GpsLocation,
    ): Location {
        return Location(
            latitude = gpsLocation.latitude,
            longitude = gpsLocation.longitude,
            accuracy = gpsLocation.accuracy,
            altitude = gpsLocation.altitude,
            speed = gpsLocation.speed,
            bearing = gpsLocation.bearing,
            timestamp = gpsLocation.timestamp,
        )
    }
}
