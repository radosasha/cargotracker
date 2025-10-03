package com.tracker.domain.repository

import com.tracker.domain.model.Location

/**
 * Repository для работы с GPS координатами
 */
interface LocationRepository {
    
    /**
     * Сохраняет GPS координату (отправляет на сервер)
     */
    suspend fun sendLocation(location: Location): Result<Unit>
    
    /**
     * Сохраняет координату в локальную БД
     * @return ID сохраненной записи
     */
    suspend fun saveLocationToDb(location: Location, batteryLevel: Float? = null): Long
    
    /**
     * Получает все неотправленные координаты из БД
     * @return Список пар (ID в БД, Location)
     */
    suspend fun getUnsentLocations(): List<Pair<Long, Location>>
    
    /**
     * Удаляет координату из БД по ID
     */
    suspend fun deleteLocationFromDb(id: Long)
    
    /**
     * Удаляет координаты из БД по списку ID
     */
    suspend fun deleteLocationsFromDb(ids: List<Long>)
    
    /**
     * Получает последнюю сохраненную координату из БД
     */
    suspend fun getLastSavedLocation(): Location?
    
    /**
     * Получает количество неотправленных координат
     */
    suspend fun getUnsentCount(): Int
}