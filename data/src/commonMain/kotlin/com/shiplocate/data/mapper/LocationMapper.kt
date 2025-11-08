package com.shiplocate.data.mapper

import com.shiplocate.data.model.LocationDataModel
import com.shiplocate.domain.model.DeviceLocation
import com.shiplocate.domain.model.GpsLocation

/**
 * Маппер для преобразования между Data и Domain моделями Location
 */
object LocationMapper {
    /**
     * Преобразует Data модель в Domain модель
     */
    fun toDomain(dataModel: LocationDataModel): GpsLocation {
        return GpsLocation(
            latitude = dataModel.latitude,
            longitude = dataModel.longitude,
            accuracy = dataModel.accuracy ?: 0f,
            altitude = dataModel.altitude,
            speed = dataModel.speed,
            bearing = dataModel.course, // course -> bearing
            timestamp = dataModel.timestamp,
        )
    }

    /**
     * Преобразует Domain модель в Data модель
     */
    fun toData(domainModel: GpsLocation): LocationDataModel {
        return LocationDataModel(
            latitude = domainModel.latitude,
            longitude = domainModel.longitude,
            timestamp = domainModel.timestamp,
            isValid = true,
            accuracy = domainModel.accuracy,
            altitude = domainModel.altitude,
            speed = domainModel.speed,
            course = domainModel.bearing, // bearing -> course
            batteryLevel = null,
        )
    }

    fun deviceLocationToData(domainModel: DeviceLocation): LocationDataModel {
        return LocationDataModel(
            latitude = domainModel.latitude,
            longitude = domainModel.longitude,
            timestamp = domainModel.timestamp,
            isValid = true,
            accuracy = domainModel.accuracy,
            altitude = domainModel.altitude,
            speed = domainModel.speed,
            course = domainModel.bearing, // bearing -> course
            batteryLevel = domainModel.batteryLevel,
        )
    }
}
