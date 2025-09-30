package com.tracker.data.datasource

import com.tracker.data.model.LocationDataModel
import kotlinx.coroutines.flow.Flow

/**
 * Data Source для работы с GPS данными
 */
interface LocationDataSource {
    
    /**
     * Сохраняет GPS координаты локально
     */
    suspend fun saveLocation(location: LocationDataModel)
    
    /**
     * Получает все сохраненные GPS координаты
     */
    suspend fun getAllLocations(): List<LocationDataModel>
    
    /**
     * Получает последние N GPS координат
     */
    suspend fun getRecentLocations(limit: Int): List<LocationDataModel>
    
    /**
     * Очищает старые GPS данные
     */
    suspend fun clearOldLocations(olderThanDays: Int)
    
    /**
     * Поток для отслеживания новых GPS координат
     */
    fun observeLocations(): Flow<LocationDataModel>
}
