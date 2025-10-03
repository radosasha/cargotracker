package com.tracker.data.datasource

import com.tracker.data.model.LocationDataModel
import com.tracker.data.model.TrackingDataStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Source для управления GPS трекингом
 */
interface TrackingDataSource {
    
    /**
     * Запускает GPS трекинг
     */
    suspend fun startTracking(): Result<Unit>
    
    /**
     * Останавливает GPS трекинг
     */
    suspend fun stopTracking(): Result<Unit>
    
    /**
     * Получает текущий статус трекинга
     */
    suspend fun getTrackingStatus(): TrackingDataStatus
}
