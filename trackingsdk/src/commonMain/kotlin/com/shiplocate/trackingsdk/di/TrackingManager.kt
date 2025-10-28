package com.shiplocate.trackingsdk.di

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.service.LocationProcessResult
import com.shiplocate.domain.service.LocationSyncService
import com.shiplocate.trackingsdk.TripRecorder
import com.shiplocate.trackingsdk.parking.ParkingTracker
import com.shiplocate.trackingsdk.parking.models.ParkingLocation
import com.shiplocate.trackingsdk.parking.models.ParkingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class TrackingManager(
    private val tripRecorder: TripRecorder,
    private val locationSyncService: LocationSyncService,
    private val parkingTracker: ParkingTracker,
    private val logger: Logger,
) {

    private val trackingScope = CoroutineScope(Dispatchers.Default)
    suspend fun startTracking(): Flow<LocationProcessResult> {
        val startResult = tripRecorder.startTracking()
        locationSyncService.startSync()

        // observe parking
        parkingTracker.observeParkingStatus().onEach {
            if (it == ParkingState.IN_PARKING) {
                parkingTracker.clear()
                tripRecorder.stopTracking()
            }
        }.launchIn(trackingScope)

        // observe new coors and add to parking tracker
        startResult.onEach {
            addToParkingTracker(it)
        }.launchIn(trackingScope)

        return startResult
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
