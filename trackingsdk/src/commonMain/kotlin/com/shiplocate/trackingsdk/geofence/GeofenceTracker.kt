package com.shiplocate.trackingsdk.geofence

import com.shiplocate.domain.repository.LoadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class GeofenceTracker(
    private val loadsRepository: LoadRepository,
    private val geofenceClient: GeofenceClient,
    private val scope: CoroutineScope,
) {
    suspend fun start(loadId: Long) {
        val stops = loadsRepository.getStopsByLoadId(loadId)
        geofenceClient.observeGeofenceEvents().onEach {
            println("hello $it")
        }.launchIn(scope)
        stops.forEach {
            geofenceClient.addGeofence(it)
        }
    }

    suspend fun stop() {
        geofenceClient.removeAllGeofences()
    }
}
