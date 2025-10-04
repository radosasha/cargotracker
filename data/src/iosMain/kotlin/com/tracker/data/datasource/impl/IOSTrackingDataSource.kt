package com.tracker.data.datasource.impl

import com.tracker.data.datasource.TrackingDataSource
import com.tracker.data.model.TrackingDataStatus
import com.tracker.domain.datasource.IOSLocationService
import com.tracker.domain.usecase.StartProcessLocationsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * iOS реализация TrackingDataSource
 */
class IOSTrackingDataSource : TrackingDataSource, KoinComponent {

    private val locationService: IOSLocationService by inject()
    private val processLocationUseCase: StartProcessLocationsUseCase by inject()
    
    private val _trackingStatusFlow = MutableSharedFlow<TrackingDataStatus>()

    private var currentStatus = TrackingDataStatus.STOPPED
    
    // Coroutine scope for processing locations
    private val scope = CoroutineScope(SupervisorJob())
    
    init {
        println("IOSTrackingDataSource: Created with LocationManager instance: ${locationService.hashCode()}")
        println("IOSTrackingDataSource: LocationManager type: ${locationService::class.simpleName}")
    }
    
    override suspend fun startTracking(): Result<Unit> {
        println("IOSTrackingDataSource: startTracking() called")
        return try {
            val result = locationService.startLocationTracking()
            if (result.isSuccess) {
                currentStatus = TrackingDataStatus.ACTIVE
                _trackingStatusFlow.emit(currentStatus)
                println("IOSTrackingDataSource: Tracking started successfully")
                
                // Запускаем обработку координат
                println("IOSTrackingDataSource: Calling startLocationProcessing()...")
                startLocationProcessing()
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
            val result = locationService.stopLocationTracking()
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
        val isActive = locationService.isLocationTrackingActive()
        return if (isActive) {
            TrackingDataStatus.ACTIVE
        } else {
            TrackingDataStatus.STOPPED
        }
    }
    
    /**
     * Запускает обработку координат через ProcessLocationUseCase
     */
    private fun startLocationProcessing() {
        println("IOSTrackingDataSource: Starting location processing...")
        println("IOSTrackingDataSource: LocationManager instance: ${locationService.hashCode()}")
        scope.launch {
            println("IOSTrackingDataSource: Collecting location updates...")
            try {
                locationService.observeLocationUpdates().collect { domainLocation ->
                    try {
                        println("IOSTrackingDataSource: 🔥 RECEIVED location in collect: Lat: ${domainLocation.latitude}, Lon: ${domainLocation.longitude}")
                        
                        // Обрабатываем координату через Use Case
                        processLocationUseCase(scope)
                        
                        println("IOSTrackingDataSource: ✅ Successfully processed location")
                        
                    } catch (e: Exception) {
                        println("IOSTrackingDataSource: Error processing location: ${e.message}")
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                println("IOSTrackingDataSource: Error in collect: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
