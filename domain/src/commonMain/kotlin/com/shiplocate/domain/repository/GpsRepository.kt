package com.shiplocate.domain.repository

import com.shiplocate.domain.model.GpsLocation
import kotlinx.coroutines.flow.Flow

interface GpsRepository {
    suspend fun startGpsTracking(): Flow<GpsLocation>

    suspend fun stopGpsTracking(): Result<Unit>
}
