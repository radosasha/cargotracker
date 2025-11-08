package com.shiplocate.data.repository

import com.shiplocate.data.datasource.LocationLocalDataSource
import com.shiplocate.data.datasource.LocationRemoteDataSource
import com.shiplocate.data.mapper.LocationEntityMapper
import com.shiplocate.data.mapper.LocationMapper
import com.shiplocate.domain.model.DeviceLocation
import com.shiplocate.domain.model.GpsLocation
import com.shiplocate.domain.repository.LocationRepository

/**
 * Реализация LocationRepository в data слое
 */
class LocationRepositoryImpl(
    private val remoteLocationDataSource: LocationRemoteDataSource,
    private val localLocationDataSource: LocationLocalDataSource,
) : LocationRepository {
    override suspend fun sendLocation(
        serverLoadId: Long,
        location: GpsLocation,
    ): Result<Unit> {
        return try {
            val locationDataModel = LocationMapper.toData(location)
            remoteLocationDataSource.sendLocation(serverLoadId, locationDataModel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendLocations(
        serverLoadId: Long,
        locations: List<DeviceLocation>,
    ): Result<Unit> {
        return try {
            val locationDataModels = locations.map { LocationMapper.deviceLocationToData(it) }
            remoteLocationDataSource.sendLocations(serverLoadId, locationDataModels)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveLocationToDb(
        location: GpsLocation,
        batteryLevel: Float?,
    ): Long {
        // Сохраняем координату с переданным уровнем батареи
        val entity = LocationEntityMapper.toEntity(location, batteryLevel)
        return localLocationDataSource.saveLocation(entity)
    }

    override suspend fun getUnsentDeviceLocations(): List<Pair<Long, DeviceLocation>> {
        val entities = localLocationDataSource.getUnsentLocations()
        return entities.map { entity ->
            Pair(entity.id, LocationEntityMapper.toDomainDeviceLocation(entity))
        }
    }

    override suspend fun deleteLocationFromDb(id: Long) {
        localLocationDataSource.markAsSentAndDelete(listOf(id))
    }

    override suspend fun deleteLocationsFromDb(ids: List<Long>) {
        localLocationDataSource.markAsSentAndDelete(ids)
    }

    override suspend fun getLastSavedLocation(): GpsLocation? {
        val entity = localLocationDataSource.getLastLocation()
        return entity?.let { LocationEntityMapper.toDomain(it) }
    }

    override suspend fun getUnsentCount(): Int {
        return localLocationDataSource.getUnsentCount()
    }
}
