package com.tracker.data.repository

import com.tracker.data.datasource.LocationDataSource
import com.tracker.data.datasource.LocationRemoteDataSource
import com.tracker.data.mapper.LocationMapper
import com.tracker.domain.model.Location
import com.tracker.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Реализация LocationRepository
 */
class LocationRepositoryImpl(
    private val localDataSource: LocationDataSource,
    private val remoteDataSource: LocationRemoteDataSource
) : LocationRepository {
    
    override suspend fun saveLocation(location: Location) {
        val dataModel = LocationMapper.toData(location)
        localDataSource.saveLocation(dataModel)
        
        // Автоматически отправляем на сервер каждые 10 координат
        val allLocations = localDataSource.getAllLocations()
        if (allLocations.size % 10 == 0) {
            syncLocationsToServer()
        }
    }
    
    override suspend fun getAllLocations(): List<Location> {
        val dataModels = localDataSource.getAllLocations()
        return LocationMapper.toDomainList(dataModels)
    }
    
    override suspend fun getRecentLocations(limit: Int): List<Location> {
        val dataModels = localDataSource.getRecentLocations(limit)
        return LocationMapper.toDomainList(dataModels)
    }
    
    override suspend fun syncLocationsToServer(): Result<Unit> {
        return try {
            val dataModels = localDataSource.getAllLocations()
            
            if (dataModels.isEmpty()) {
                return Result.success(Unit)
            }
            
            val result = remoteDataSource.sendLocations(dataModels)
            
            if (result.isSuccess) {
                // Очищаем отправленные данные
                localDataSource.clearOldLocations(0) // Очищаем все
                println("Successfully synced ${dataModels.size} locations to server")
            }
            
            result
        } catch (e: Exception) {
            println("Failed to sync locations: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun clearOldLocations(olderThanDays: Int) {
        localDataSource.clearOldLocations(olderThanDays)
    }
    
    override fun observeLocations(): Flow<Location> {
        return localDataSource.observeLocations().map { dataModel ->
            LocationMapper.toDomain(dataModel)
        }
    }
}
