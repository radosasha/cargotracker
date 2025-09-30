package com.tracker.data.datasource.impl

// import com.tracker.IOSLocationManager - заглушка
import com.tracker.data.datasource.TrackingDataSource
import com.tracker.data.model.LocationDataModel
import com.tracker.data.model.TrackingDataStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * iOS реализация TrackingDataSource
 */
class IOSTrackingDataSource : TrackingDataSource {

    // Заглушка для IOSLocationManager
    private fun startLocationTracking() {}
    private fun stopLocationTracking() {}
    private fun isLocationTrackingActive(): Boolean = false
    
    private val _trackingStatusFlow = MutableSharedFlow<TrackingDataStatus>()
    private val _locationFlow = MutableSharedFlow<LocationDataModel>()

    private var currentStatus = TrackingDataStatus.STOPPED
    
    override suspend fun startTracking(): Result<Unit> {
        return try {
            startLocationTracking()
            currentStatus = TrackingDataStatus.ACTIVE
            _trackingStatusFlow.emit(currentStatus)
            Result.success(Unit)
        } catch (e: Exception) {
            currentStatus = TrackingDataStatus.ERROR
            _trackingStatusFlow.emit(currentStatus)
            Result.failure(e)
        }
    }
    
    override suspend fun stopTracking(): Result<Unit> {
        return try {
            stopLocationTracking()
            currentStatus = TrackingDataStatus.STOPPED
            _trackingStatusFlow.emit(currentStatus)
            Result.success(Unit)
        } catch (e: Exception) {
            currentStatus = TrackingDataStatus.ERROR
            _trackingStatusFlow.emit(currentStatus)
            Result.failure(e)
        }
    }
    
    override suspend fun getTrackingStatus(): TrackingDataStatus {
        return if (isLocationTrackingActive()) {
            TrackingDataStatus.ACTIVE
        } else {
            TrackingDataStatus.STOPPED
        }
    }
    
    override fun observeTrackingStatus(): Flow<TrackingDataStatus> {
        return _trackingStatusFlow.asSharedFlow()
    }
    
    override fun observeLocationUpdates(): Flow<LocationDataModel> {
        return _locationFlow.asSharedFlow()
    }
}
