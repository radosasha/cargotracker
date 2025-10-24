package com.shiplocate.domain.repository

import com.shiplocate.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface GpsRepository {
    fun startGpsTracking(): Flow<Location>

    suspend fun stopGpsTracking(): Result<Unit>
}
