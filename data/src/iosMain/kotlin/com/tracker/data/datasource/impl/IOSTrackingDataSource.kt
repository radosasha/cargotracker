package com.tracker.data.datasource.impl

import com.tracker.data.datasource.TrackingDataSource
import com.tracker.data.model.TrackingDataStatus
import com.tracker.domain.datasource.TrackingRequester
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * iOS реализация TrackingDataSource
 */
class IOSTrackingDataSource : TrackingDataSource, KoinComponent {
    
    private val trackingRequester: TrackingRequester by inject()
    
    private val _trackingStatusFlow = MutableSharedFlow<TrackingDataStatus>()
    private var currentStatus = TrackingDataStatus.STOPPED
    
    override suspend fun startTracking(): Result<Unit> {
        return try {
            val result = trackingRequester.startTracking()
            if (result.isSuccess) {
                currentStatus = TrackingDataStatus.ACTIVE
                _trackingStatusFlow.emit(currentStatus)
            }
            result
        } catch (e: Exception) {
            currentStatus = TrackingDataStatus.ERROR
            _trackingStatusFlow.emit(currentStatus)
            Result.failure(e)
        }
    }
    
    override suspend fun stopTracking(): Result<Unit> {
        return try {
            val result = trackingRequester.stopTracking()
            if (result.isSuccess) {
                currentStatus = TrackingDataStatus.STOPPED
                _trackingStatusFlow.emit(currentStatus)
            }
            result
        } catch (e: Exception) {
            currentStatus = TrackingDataStatus.ERROR
            _trackingStatusFlow.emit(currentStatus)
            Result.failure(e)
        }
    }
    
    override suspend fun getTrackingStatus(): TrackingDataStatus {
        return if (trackingRequester.isTrackingActive()) {
            TrackingDataStatus.ACTIVE
        } else {
            TrackingDataStatus.STOPPED
        }
    }
}
