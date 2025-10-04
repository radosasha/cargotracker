package com.tracker.data.repository

import com.tracker.data.datasource.GpsLocationDataSource
import com.tracker.data.datasource.LocationLocalDataSource
import com.tracker.data.datasource.LocationRemoteDataSource
import com.tracker.data.mapper.GpsLocationMapper
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
    private val remoteLocationDataSource: LocationRemoteDataSource,
    private val localLocationDataSource: LocationLocalDataSource,
    private val gpsLocationDataSource: GpsLocationDataSource,
    private val deviceId: String
) : LocationRepository {
    
    override suspend fun sendLocation(location: Location): Result<Unit> {
        return try {
            val locationDataModel = LocationMapper.toData(location)
            remoteLocationDataSource.sendLocation(locationDataModel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun sendLocations(locations: List<Location>): Result<Unit> {
        return try {
            val locationDataModels = locations.map { LocationMapper.toData(it) }
            remoteLocationDataSource.sendLocations(locationDataModels)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun saveLocationToDb(location: Location, batteryLevel: Float?): Long {
        // Сохраняем координату с переданным уровнем батареи
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

    override fun startGpsTracking(): Flow<Location> {
        return gpsLocationDataSource.startGpsTracking().map { gpsLocation ->
            GpsLocationMapper.toDomain(gpsLocation, deviceId)
        }
    }
    
    override suspend fun stopGpsTracking(): Result<Unit> {
        return gpsLocationDataSource.stopGpsTracking()
    }
}