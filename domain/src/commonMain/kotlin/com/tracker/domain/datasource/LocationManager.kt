package com.tracker.domain.datasource

import com.tracker.domain.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс для управления GPS координатами
 */
interface LocationManager {
    
    /**
     * Запускает GPS трекинг
     */
    fun startLocationTracking(): Result<Unit>
    
    /**
     * Останавливает GPS трекинг
     */
    fun stopLocationTracking(): Result<Unit>
    
    /**
     * Проверяет, активен ли трекинг
     */
    fun isLocationTrackingActive(): Boolean
    
    /**
     * Наблюдает за GPS координатами
     */
    fun observeLocationUpdates(): Flow<Location>
}
