package com.shiplocate.data.repository

import com.shiplocate.data.datasource.LocationLocalDataSource
import com.shiplocate.data.datasource.LocationRemoteDataSource
import com.shiplocate.data.mapper.LocationEntityMapper
import com.shiplocate.data.mapper.LocationMapper
import com.shiplocate.domain.model.Location
import com.shiplocate.domain.repository.LocationRepository

/**
 * Реализация LocationRepository в data слое
 */
class LocationRepositoryImpl(
    private val remoteLocationDataSource: LocationRemoteDataSource,
    private val localLocationDataSource: LocationLocalDataSource,
) : LocationRepository {
    override suspend fun sendLocation(
        loadId: String,
        location: Location,
    ): Result<Unit> {
        return try {
            val locationDataModel = LocationMapper.toData(location)
            remoteLocationDataSource.sendLocation(loadId, locationDataModel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendLocations(
        loadId: String,
        locations: List<Location>,
    ): Result<Unit> {
        return try {
            val locationDataModels = locations.map { LocationMapper.toData(it) }
            remoteLocationDataSource.sendLocations(loadId, locationDataModels)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveLocationToDb(
        location: Location,
        batteryLevel: Float?,
    ): Long {
        // Сохраняем координату с переданным уровнем батареи
        val entity = LocationEntityMapper.toEntity(location, batteryLevel)
        return localLocationDataSource.saveLocation(entity)
    }

    override suspend fun getUnsentLocations(loadId: String): List<Pair<Long, Location>> {
        val entities = localLocationDataSource.getUnsentLocations()
        return entities.map { entity ->
            Pair(entity.id, LocationEntityMapper.toDomain(entity, loadId))
        }
    }

    override suspend fun deleteLocationFromDb(id: Long) {
        localLocationDataSource.markAsSentAndDelete(listOf(id))
    }

    override suspend fun deleteLocationsFromDb(ids: List<Long>) {
        localLocationDataSource.markAsSentAndDelete(ids)
    }

    override suspend fun getLastSavedLocation(loadId: String): Location? {
        val entity = localLocationDataSource.getLastLocation()
        return entity?.let { LocationEntityMapper.toDomain(it, loadId) }
    }

    override suspend fun getUnsentCount(): Int {
        return localLocationDataSource.getUnsentCount()
    }
}
