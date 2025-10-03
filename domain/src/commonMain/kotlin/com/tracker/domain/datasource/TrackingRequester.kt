package com.tracker.domain.datasource

import com.tracker.domain.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс для GPS трекинга
 */
interface TrackingRequester {
    suspend fun startTracking(): Result<Unit>
    suspend fun stopTracking(): Result<Unit>
    suspend fun isTrackingActive(): Boolean
}
