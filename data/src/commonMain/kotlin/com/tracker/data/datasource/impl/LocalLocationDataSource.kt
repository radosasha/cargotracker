package com.tracker.data.datasource.impl

import com.tracker.data.datasource.LocationDataSource
import com.tracker.data.model.LocationDataModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.minus
import kotlin.time.Duration.Companion.days

/**
 * Локальная реализация LocationDataSource
 * В реальном приложении здесь будет база данных (SQLDelight)
 */
class LocalLocationDataSource : LocationDataSource {
    
    // Временное хранилище в памяти
    private val locations = mutableListOf<LocationDataModel>()
    private val _locationFlow = MutableSharedFlow<LocationDataModel>()
    private val locationFlow = _locationFlow.asSharedFlow()
    
    override suspend fun saveLocation(location: LocationDataModel) {
        locations.add(location)
        _locationFlow.emit(location)
    }
    
    override suspend fun getAllLocations(): List<LocationDataModel> {
        return locations.toList()
    }
    
    override suspend fun getRecentLocations(limit: Int): List<LocationDataModel> {
        return locations.takeLast(limit)
    }
    
    override suspend fun clearOldLocations(olderThanDays: Int) {
        if (olderThanDays <= 0) {
            locations.clear()
        } else {
            val cutoffTime = Clock.System.now().minus(olderThanDays.days)
            locations.removeAll { location ->
                location.timestamp < cutoffTime
            }
        }
    }
    
    override fun observeLocations(): Flow<LocationDataModel> {
        return locationFlow
    }
}
