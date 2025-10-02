package com.tracker.data.repository

import com.tracker.data.datasource.LocalLocationDataSource
import com.tracker.data.datasource.LocationDataSource
import com.tracker.data.mapper.LocationEntityMapper
import com.tracker.data.mapper.LocationMapper
import com.tracker.domain.model.Location
import com.tracker.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Реализация LocationRepository в data слое
 */
class LocationRepositoryImpl(
    private val locationDataSource: LocationDataSource,
    private val localLocationDataSource: LocalLocationDataSource,
    private val deviceId: String
) : LocationRepository {
    
    override suspend fun saveLocation(location: Location): Result<Unit> {
        return try {
            val locationDataModel = LocationMapper.toData(location)
            locationDataSource.saveLocation(locationDataModel)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAllLocations(): List<Location> {
        val dataModels = locationDataSource.getAllLocations()
        return LocationMapper.toDomainList(dataModels)
    }
    
    override suspend fun getRecentLocations(limit: Int): List<Location> {
        val dataModels = locationDataSource.getRecentLocations(limit)
        return LocationMapper.toDomainList(dataModels)
    }
    
    override suspend fun clearOldLocations(olderThanDays: Int) {
        locationDataSource.clearOldLocations(olderThanDays)
    }
    
    override fun observeLocations(): Flow<Location> {
        return locationDataSource.observeLocations().map { dataModel ->
            LocationMapper.toDomain(dataModel)
        }
    }
    
    override suspend fun saveLocationToDb(location: Location, batteryLevel: Float?): Long {
        val entity = LocationEntityMapper.toEntity(location, batteryLevel)
        return localLocationDataSource.saveLocation(entity)
    }
    
    override suspend fun getUnsentLocations(): List<Pair<Long, Location>> {
        val entities = localLocationDataSource.getUnsentLocations()
        return entities.map { entity ->
            Pair(entity.id, LocationEntityMapper.toDomain(entity, deviceId))
        }
    }
    
    override suspend fun deleteLocationFromDb(id: Long) {
        localLocationDataSource.markAsSentAndDelete(listOf(id))
    }
    
    override suspend fun deleteLocationsFromDb(ids: List<Long>) {
        localLocationDataSource.markAsSentAndDelete(ids)
    }
    
    override suspend fun getLastSavedLocation(): Location? {
        val entity = localLocationDataSource.getLastLocation()
        return entity?.let { LocationEntityMapper.toDomain(it, deviceId) }
    }
    
    override suspend fun getUnsentCount(): Int {
        return localLocationDataSource.getUnsentCount()
    }
}