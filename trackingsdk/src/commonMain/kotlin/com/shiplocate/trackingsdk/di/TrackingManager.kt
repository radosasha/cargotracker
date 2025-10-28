package com.shiplocate.trackingsdk.di

import com.shiplocate.domain.service.LocationProcessResult
import com.shiplocate.domain.usecase.StartTrackerUseCase
import com.shiplocate.domain.usecase.StopTrackerUseCase
import kotlinx.coroutines.flow.Flow

class TrackingManager(
    private val startTrackerUseCase: StartTrackerUseCase,
    private val stopTrackerUseCase: StopTrackerUseCase,
) {

    suspend fun startTracking(): Flow<LocationProcessResult> {
        return startTrackerUseCase()
    }


    suspend fun stopTracking(): Result<Unit> {
        return stopTrackerUseCase()
    }
}
