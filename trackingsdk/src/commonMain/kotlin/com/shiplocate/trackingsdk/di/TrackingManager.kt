package com.shiplocate.trackingsdk.di

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.service.LocationProcessResult
import com.shiplocate.domain.service.LocationSyncService
import com.shiplocate.trackingsdk.TripRecorder
import com.shiplocate.trackingsdk.parking.ParkingTracker
import com.shiplocate.trackingsdk.parking.models.ParkingLocation
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

        startResult.onEach {
            // Передаем координаты в ParkingTracker для анализа парковки
            val parkingLocation = ParkingLocation(
                it.lastCoordinateLat,
                it.lastCoordinateLon,
                it.lastCoordinateTime,
                it.coordinateErrorMeters
            )
            val isInParking = parkingTracker.addCoordinate(parkingLocation)
            logger.debug(LogCategory.LOCATION, "TrackingManager: Parking status: $isInParking")

        }.launchIn(trackingScope)

        return startResult
    }

    suspend fun stopTracking(): Result<Unit> {
        locationSyncService.stopSync()
        parkingTracker.clear()
        return tripRecorder.stopTracking()
    }
}
