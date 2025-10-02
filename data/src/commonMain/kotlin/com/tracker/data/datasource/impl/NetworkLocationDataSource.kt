package com.tracker.data.datasource.impl

import com.tracker.data.datasource.LocationDataSource
import com.tracker.data.model.LocationDataModel
import com.tracker.data.network.api.LocationApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Сетевой источник данных для отправки координат на сервер
 */
class NetworkLocationDataSource : LocationDataSource, KoinComponent {
    
    private val locationApi: LocationApi by inject()
    
    private val _locationFlow = MutableSharedFlow<LocationDataModel>()
    private val locationFlow = _locationFlow.asSharedFlow()
    
    override suspend fun saveLocation(location: LocationDataModel) {
        println("NetworkLocationDataSource.saveLocation() - saving location to server")
        
        val result = locationApi.sendLocation(location)
        result.fold(
            onSuccess = {
                println("NetworkLocationDataSource.saveLocation() - success")
                _locationFlow.emit(location)
            },
            onFailure = { error ->
                println("NetworkLocationDataSource.saveLocation() - failed: ${error.message}")
                throw error
            }
        )
    }
    
    override suspend fun getAllLocations(): List<LocationDataModel> {
        // Для сетевого источника данных этот метод не применим
        // Данные хранятся на сервере
        return emptyList()
    }
    
    override suspend fun getRecentLocations(limit: Int): List<LocationDataModel> {
        // Для сетевого источника данных этот метод не применим
        return emptyList()
    }
    
    override suspend fun clearOldLocations(olderThanDays: Int) {
        // Для сетевого источника данных этот метод не применим
        // Очистка данных происходит на сервере
    }
    
    override fun observeLocations(): Flow<LocationDataModel> {
        return locationFlow
    }
}
