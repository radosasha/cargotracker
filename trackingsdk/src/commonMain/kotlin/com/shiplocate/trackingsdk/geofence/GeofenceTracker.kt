package com.shiplocate.trackingsdk.geofence

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.load.Stop
import com.shiplocate.domain.repository.AuthPreferencesRepository
import com.shiplocate.domain.repository.LoadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class GeofenceTracker(
    private val loadsRepository: LoadRepository,
    private val geofenceClient: GeofenceClient,
    private val authPreferencesRepository: AuthPreferencesRepository,
    private val logger: Logger,
    private val scope: CoroutineScope,
) {
    private var observeGeofenceJob: Job? = null
    private val trackedStopIds = mutableListOf<Long>()

    suspend fun start(loadId: Long) {
        observeGeofenceEvents()
        val fetchedStops = loadsRepository.getNotEnteredStopsByLoadId(loadId)
        mergeStops(fetchedStops)
        loadsRepository.observeNotEnteredStopIdsUpdates().onEach { updatedStops ->
            mergeStops(updatedStops)
        }.launchIn(scope)
    }

    private suspend fun mergeStops(stops: List<Stop>) {
        // Find stops to add (in updatedStops but not in trackedStopIds)
        val stopsToAdd = stops.map { it.id } - trackedStopIds
        stopsToAdd.forEach { stopId ->
            val stop = stops.find { it.id == stopId } ?: throw IllegalStateException("Stop not found: $stopId")
            addStopToGeofence(stopId, stop.latitude, stop.longitude, stop.geofenceRadius)
        }

        // Find stops to remove (in trackedStopIds but not in updatedStops)
        val stopsToRemove = trackedStopIds - stops.map { it.id }
        stopsToRemove.forEach { stopId ->
            removeStopFromGeofence(stopId)
        }
    }

    private fun observeGeofenceEvents() {
        observeGeofenceJob = geofenceClient.observeGeofenceEvents().onEach { event ->
            when (event) {
                is GeofenceEvent.Entered -> {
                    logger.info(
                        LogCategory.LOCATION,
                        "GeofenceTracker: Entered geofence for stopId=${event.stopId}, stopType=${event.stopType}",
                    )

                    // remove stop id from geofence
                    removeStopFromGeofence(event.stopId)

                    // Save stopId to queue if it doesn't exist
                    loadsRepository.addStopIdToQueue(event.stopId)

                    // Send queue to server
                    sendEnterStopQueue()
                }

                is GeofenceEvent.Exited -> {
                    logger.info(
                        LogCategory.LOCATION,
                        "GeofenceTracker: Exited geofence for stopId=${event.stopId}, stopType=${event.stopType}",
                    )
                    // We only handle ENTER events for enterstop API
                }
            }
        }.launchIn(scope)
    }

    private suspend fun removeStopFromGeofence(stopId: Long) {
        geofenceClient.removeGeofence(stopId)
        trackedStopIds.remove(stopId)
    }

    private suspend fun addStopToGeofence(stopId: Long, latitude: Double, longitude: Double, radius: Int) {
        geofenceClient.addGeofence(stopId, latitude, longitude, radius)
        trackedStopIds.add(stopId)
    }

    suspend fun stop() {
        observeGeofenceJob?.cancel()
        observeGeofenceJob = null
        geofenceClient.removeAllGeofences()
        trackedStopIds.clear()
    }

    private suspend fun sendEnterStopQueue() {
        try {
            // Get authentication token
            val authSession = authPreferencesRepository.getSession()
            val token = authSession?.token

            if (token == null) {
                logger.warn(
                    LogCategory.LOCATION,
                    "GeofenceTracker: No authentication token available, cannot send enter stop queue",
                )
                return
            }

            // Send queue to server
            val result = loadsRepository.sendEnterStopQueue(token)
            if (result.isSuccess) {
                logger.info(LogCategory.LOCATION, "GeofenceTracker: Successfully sent enter stop queue")
            } else {
                logger.error(
                    LogCategory.LOCATION,
                    "GeofenceTracker: Failed to send enter stop queue: ${result.exceptionOrNull()?.message}",
                )
            }
        } catch (e: Exception) {
            logger.error(LogCategory.LOCATION, "GeofenceTracker: Error sending enter stop queue: ${e.message}", e)
        }
    }
}
