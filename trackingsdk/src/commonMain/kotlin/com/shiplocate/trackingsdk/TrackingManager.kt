package com.shiplocate.trackingsdk

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.service.LocationProcessResult
import com.shiplocate.domain.service.LocationSyncService
import com.shiplocate.trackingsdk.parking.ParkingTracker
import com.shiplocate.trackingsdk.parking.models.ParkingLocation
import com.shiplocate.trackingsdk.parking.models.ParkingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class TrackingManager(
    private val tripRecorder: TripRecorder,
    private val locationSyncService: LocationSyncService,
    private val parkingTracker: ParkingTracker,
    private val logger: Logger,
) {
    private var currentState = TrackingState.TRIP_RECORDING
    private val trackingScope = CoroutineScope(Dispatchers.Default)
    private val trackingState = MutableSharedFlow<LocationProcessResult>(replay = 0)

    private var parkingStateJob: Job? = null
    private var tripCoordinatedJob: Job? = null

    suspend fun startTracking(): Flow<LocationProcessResult> {
        locationSyncService.startSync()
        switchToState(currentState)
        return trackingState
    }

    private suspend fun switchToState(state: TrackingState) {
        when (state) {
            TrackingState.IN_PARKING -> {
                tripCoordinatedJob?.cancel()
                parkingStateJob?.cancel()
                tripRecorder.stopTracking()
            }

            TrackingState.TRIP_RECORDING -> {
                // observe trip coordinates
                tripCoordinatedJob = tripRecorder.startTracking().onEach {
                    addToParkingTracker(it)
                    trackingState.emit(it)
                }.launchIn(trackingScope)

                // observe parking status
                parkingStateJob = parkingTracker.observeParkingStatus().onEach {
                    if (it == ParkingState.IN_PARKING) {
                        currentState = TrackingState.IN_PARKING
                        logger.info(LogCategory.LOCATION, "TrackingManager: User entered parking, stopping tracking")
                        switchToState(TrackingState.IN_PARKING)
                    }
                }.launchIn(trackingScope)

                // observe parking timeout
                parkingTracker.observeParkingTimeout().onEach {
                    logger.info(LogCategory.LOCATION, "TrackingManager: Parking finished event received")
                    currentState = TrackingState.IN_PARKING
                    switchToState(TrackingState.IN_PARKING)
                }.launchIn(trackingScope)
            }
        }
    }

    suspend fun addToParkingTracker(location: LocationProcessResult) {
        // Передаем координаты в ParkingTracker для анализа парковки
        val parkingLocation = ParkingLocation(
            location.lastCoordinateLat,
            location.lastCoordinateLon,
            location.lastCoordinateTime,
            location.coordinateErrorMeters
        )
        val isInParking = parkingTracker.addCoordinate(parkingLocation)
        logger.debug(LogCategory.LOCATION, "TrackingManager: Parking status: $isInParking")
    }

    suspend fun stopTracking(): Result<Unit> {
        locationSyncService.stopSync()
        parkingTracker.clear()
        return tripRecorder.stopTracking()
    }
}
