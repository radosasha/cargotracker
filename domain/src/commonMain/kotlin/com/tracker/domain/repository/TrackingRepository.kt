package com.tracker.domain.repository

import com.tracker.domain.model.Location
import com.tracker.domain.model.TrackingStatus
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс репозитория для управления GPS трекингом
 */
interface TrackingRepository {
    
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
    suspend fun getTrackingStatus(): TrackingStatus
}
