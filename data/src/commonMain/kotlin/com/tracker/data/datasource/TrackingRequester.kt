package com.tracker.data.datasource

/**
 * Интерфейс для GPS трекинга
 * Находится в data слое, так как это деталь реализации (DataSource)
 */
interface TrackingRequester {
    suspend fun startTracking(): Result<Unit>
    suspend fun stopTracking(): Result<Unit>
    suspend fun isTrackingActive(): Boolean
}
