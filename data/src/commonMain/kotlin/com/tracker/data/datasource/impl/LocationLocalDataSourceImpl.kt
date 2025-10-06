package com.tracker.data.datasource.impl

import com.tracker.core.database.TrackerDatabase
import com.tracker.core.database.entity.LocationEntity
import com.tracker.data.datasource.LocationLocalDataSource
import kotlinx.coroutines.flow.Flow

/**
 * Реализация LocalLocationDataSource на основе Room Database
 */
class LocationLocalDataSourceImpl(
    private val database: TrackerDatabase
) : LocationLocalDataSource {
    
    private val locationDao = database.locationDao()
    
    override suspend fun saveLocation(location: LocationEntity): Long {
        return locationDao.insert(location)
    }
    
    override suspend fun getUnsentLocations(): List<LocationEntity> {
        return locationDao.getUnsentLocations()
    }
    
    override suspend fun getUnsentCount(): Int {
        return locationDao.getUnsentCount()
    }
    
    override suspend fun getLastLocation(): LocationEntity? {
        return locationDao.getLastLocation()
    }
    
    override suspend fun getLastUnsentLocation(): LocationEntity? {
        return locationDao.getLastUnsentLocation()
    }
    
    override suspend fun markAsSentAndDelete(ids: List<Long>) {
        locationDao.markAsSentAndDelete(ids)
    }
    
    override suspend fun deleteOlderThan(beforeTimestamp: Long) {
        locationDao.deleteOlderThan(beforeTimestamp)
    }
    
    override fun observeUnsentCount(): Flow<Int> {
        return locationDao.observeUnsentCount()
    }
}

