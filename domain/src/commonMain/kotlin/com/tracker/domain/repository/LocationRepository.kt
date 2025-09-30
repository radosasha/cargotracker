package com.tracker.domain.repository

import com.tracker.domain.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс репозитория для работы с GPS данными
 */
interface LocationRepository {
    
    /**
     * Сохраняет GPS координаты локально
     */
    suspend fun saveLocation(location: Location)
    
    /**
     * Получает все сохраненные GPS координаты
     */
    suspend fun getAllLocations(): List<Location>
    
    /**
     * Получает последние N GPS координат
     */
    suspend fun getRecentLocations(limit: Int = 100): List<Location>
    
    /**
     * Отправляет накопленные GPS данные на сервер
     */
    suspend fun syncLocationsToServer(): Result<Unit>
    
    /**
     * Очищает старые GPS данные
     */
    suspend fun clearOldLocations(olderThanDays: Int = 7)
    
    /**
     * Поток для отслеживания новых GPS координат
     */
    fun observeLocations(): Flow<Location>
}
