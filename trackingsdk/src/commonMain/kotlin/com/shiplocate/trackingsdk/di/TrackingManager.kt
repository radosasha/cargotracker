package com.shiplocate.trackingsdk.di

import com.shiplocate.domain.service.LocationProcessResult
import com.shiplocate.trackingsdk.TripRecorder
import kotlinx.coroutines.flow.Flow

class TrackingManager(
    private val tripRecorder: TripRecorder,
) {

    suspend fun startTracking(): Flow<LocationProcessResult> {
        return tripRecorder.startTracking()
    }

    suspend fun stopTracking(): Result<Unit> {
        return tripRecorder.stopTracking()
    }
}
