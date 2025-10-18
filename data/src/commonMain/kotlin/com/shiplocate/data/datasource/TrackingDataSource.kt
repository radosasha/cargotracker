package com.shiplocate.data.datasource

import com.shiplocate.data.model.TrackingDataStatus

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
