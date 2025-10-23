package com.shiplocate.data.mapper

import com.shiplocate.data.model.LocationDataModel
import com.shiplocate.domain.model.Location

/**
 * Маппер для преобразования между Data и Domain моделями Location
 */
object LocationMapper {
    /**
     * Преобразует Data модель в Domain модель
     */
    fun toDomain(dataModel: LocationDataModel): Location {
        return Location(
            latitude = dataModel.latitude,
            longitude = dataModel.longitude,
            accuracy = dataModel.accuracy ?: 0f,
            altitude = dataModel.altitude,
            speed = dataModel.speed,
            bearing = dataModel.course, // course -> bearing
            timestamp = dataModel.timestamp,
            deviceId = dataModel.deviceId,
        )
    }

    /**
     * Преобразует Domain модель в Data модель
     */
    fun toData(domainModel: Location): LocationDataModel {
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
            deviceId = domainModel.deviceId,
        )
    }

    /**
     * Преобразует список Data моделей в список Domain моделей
     */
    fun toDomainList(dataModels: List<LocationDataModel>): List<Location> {
        return dataModels.map { toDomain(it) }
    }

    /**
     * Преобразует список Domain моделей в список Data моделей
     */
    fun toDataList(domainModels: List<Location>): List<LocationDataModel> {
        return domainModels.map { toData(it) }
    }
}
