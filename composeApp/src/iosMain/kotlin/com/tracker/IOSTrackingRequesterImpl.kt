package com.tracker

import com.tracker.domain.datasource.TrackingRequester
import com.tracker.domain.model.Location
import com.tracker.IOSLocationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * iOS реализация TrackingRequester
 */
class IOSTrackingRequesterImpl : TrackingRequester, KoinComponent {
    
    private val locationService: IOSLocationService by inject()
    
    private val _locationFlow = MutableSharedFlow<Location>()
    private var isTracking = false
    
    override suspend fun startTracking(): Result<Unit> {
        return try {
            val result = locationService.startLocationProcessing()
            if (result.isSuccess) {
                isTracking = true
                println("IOSTrackingRequesterImpl: Tracking started, isTracking = $isTracking")
            } else {
                println("IOSTrackingRequesterImpl: Failed to start tracking: ${result.exceptionOrNull()?.message}")
            }
            result
        } catch (e: Exception) {
            println("IOSTrackingRequesterImpl: Error starting tracking: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun stopTracking(): Result<Unit> {
        return try {
            val result = locationService.stopLocationProcessing()
            if (result.isSuccess) {
                isTracking = false
                println("IOSTrackingRequesterImpl: Tracking stopped, isTracking = $isTracking")
            } else {
                println("IOSTrackingRequesterImpl: Failed to stop tracking: ${result.exceptionOrNull()?.message}")
            }
            result
        } catch (e: Exception) {
            println("IOSTrackingRequesterImpl: Error stopping tracking: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun isTrackingActive(): Boolean {
        val isActive = locationService.isLocationProcessingActive()
        println("IOSTrackingRequesterImpl: isTrackingActive() = $isActive")
        return isActive
    }
    
    override fun observeLocationUpdates(): Flow<Location> {
        return _locationFlow.asSharedFlow()
    }
}
