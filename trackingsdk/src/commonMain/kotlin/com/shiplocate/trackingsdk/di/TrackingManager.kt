package com.shiplocate.trackingsdk.di

import com.shiplocate.domain.service.LocationProcessResult
import com.shiplocate.domain.service.LocationSyncService
import com.shiplocate.trackingsdk.TripRecorder
import kotlinx.coroutines.flow.Flow

class TrackingManager(
    private val tripRecorder: TripRecorder,
    private val locationSyncService: LocationSyncService,
) {

    suspend fun startTracking(): Flow<LocationProcessResult> {
        val startResult = tripRecorder.startTracking()
        locationSyncService.startSync()
        return startResult
    }

    suspend fun stopTracking(): Result<Unit> {
        locationSyncService.stopSync()
        return tripRecorder.stopTracking()
    }
}
