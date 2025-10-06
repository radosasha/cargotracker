package com.tracker.data.datasource

import com.tracker.core.database.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Source для работы с локальным хранилищем координат
 */
interface LocationLocalDataSource {
    
    /**
     * Сохраняет координату в локальную БД
     */
    suspend fun saveLocation(location: LocationEntity): Long
    
    /**
     * Получает все неотправленные координаты
     */
    suspend fun getUnsentLocations(): List<LocationEntity>
    
    /**
     * Получает количество неотправленных координат
     */
    suspend fun getUnsentCount(): Int
    
    /**
     * Получает последнюю сохраненную координату
     */
    suspend fun getLastLocation(): LocationEntity?
    
    /**
     * Получает последнюю неотправленную координату
     */
    suspend fun getLastUnsentLocation(): LocationEntity?
    
    /**
     * Помечает координаты как отправленные и удаляет их
     */
    suspend fun markAsSentAndDelete(ids: List<Long>)
    
    /**
     * Удаляет старые координаты (старше указанного времени в миллисекундах)
     */
    suspend fun deleteOlderThan(beforeTimestamp: Long)
    
    /**
     * Наблюдает за количеством неотправленных координат
     */
    fun observeUnsentCount(): Flow<Int>
}

