package com.tracker.data.datasource.impl

import com.tracker.data.datasource.TrackingDataSource
import com.tracker.data.mapper.LocationMapper
import com.tracker.data.model.LocationDataModel
import com.tracker.data.model.TrackingDataStatus
import com.tracker.domain.datasource.LocationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * iOS реализация TrackingDataSource
 */
class IOSTrackingDataSource : TrackingDataSource, KoinComponent {

    private val locationManager: LocationManager by inject()
    
    private val _trackingStatusFlow = MutableSharedFlow<TrackingDataStatus>()
    private val _locationFlow = MutableSharedFlow<LocationDataModel>()

    private var currentStatus = TrackingDataStatus.STOPPED
    
    override suspend fun startTracking(): Result<Unit> {
        return try {
            val result = locationManager.startLocationTracking()
            if (result.isSuccess) {
                currentStatus = TrackingDataStatus.ACTIVE
                _trackingStatusFlow.emit(currentStatus)
                println("IOSTrackingDataSource: Tracking started successfully")
            } else {
                currentStatus = TrackingDataStatus.ERROR
                _trackingStatusFlow.emit(currentStatus)
                println("IOSTrackingDataSource: Failed to start tracking: ${result.exceptionOrNull()?.message}")
            }
            result
        } catch (e: Exception) {
            currentStatus = TrackingDataStatus.ERROR
            _trackingStatusFlow.emit(currentStatus)
            println("IOSTrackingDataSource: Exception starting tracking: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun stopTracking(): Result<Unit> {
        return try {
            val result = locationManager.stopLocationTracking()
            if (result.isSuccess) {
                currentStatus = TrackingDataStatus.STOPPED
                _trackingStatusFlow.emit(currentStatus)
                println("IOSTrackingDataSource: Tracking stopped successfully")
            } else {
                currentStatus = TrackingDataStatus.ERROR
                _trackingStatusFlow.emit(currentStatus)
                println("IOSTrackingDataSource: Failed to stop tracking: ${result.exceptionOrNull()?.message}")
            }
            result
        } catch (e: Exception) {
            currentStatus = TrackingDataStatus.ERROR
            _trackingStatusFlow.emit(currentStatus)
            println("IOSTrackingDataSource: Exception stopping tracking: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun getTrackingStatus(): TrackingDataStatus {
        val isActive = locationManager.isLocationTrackingActive()
        return if (isActive) {
            TrackingDataStatus.ACTIVE
        } else {
            TrackingDataStatus.STOPPED
        }
    }
    
    override fun observeTrackingStatus(): Flow<TrackingDataStatus> {
        return _trackingStatusFlow.asSharedFlow()
    }
    
    override fun observeLocationUpdates(): Flow<LocationDataModel> {
        return locationManager.observeLocationUpdates().map { domainLocation ->
            LocationMapper.toData(domainLocation)
        }
    }
}
