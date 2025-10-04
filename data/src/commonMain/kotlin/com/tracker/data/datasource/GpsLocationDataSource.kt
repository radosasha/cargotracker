package com.tracker.data.datasource

import com.tracker.data.model.GpsLocation
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс для работы с GPS координатами
 * Обертка над GpsManager для работы с GPS данными
 */
interface GpsLocationDataSource {
    
    /**
     * Запускает GPS трекинг и возвращает поток координат
     * @return Flow<GpsLocation> - поток GPS координат
     */
    fun startGpsTracking(): Flow<GpsLocation>
    
    /**
     * Останавливает GPS трекинг
     * @return Result<Unit> - результат операции
     */
    suspend fun stopGpsTracking(): Result<Unit>
}

