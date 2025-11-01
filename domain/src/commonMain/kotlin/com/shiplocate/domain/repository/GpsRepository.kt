package com.shiplocate.domain.repository

import com.shiplocate.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface GpsRepository {
    suspend fun startGpsTracking(): Flow<Location>

    suspend fun stopGpsTracking(): Result<Unit>
}
