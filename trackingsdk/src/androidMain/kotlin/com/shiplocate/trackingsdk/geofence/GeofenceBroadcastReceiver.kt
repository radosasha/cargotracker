package com.shiplocate.trackingsdk.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin

/**
 * BroadcastReceiver для получения событий геозон от GeofencingClient
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val logger: Logger = getKoin().get()
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null) {
            logger.warn(LogCategory.LOCATION, "GeofenceBroadcastReceiver: GeofencingEvent is null")
            return
        }

        if (geofencingEvent.hasError()) {
            val errorCode = geofencingEvent.errorCode
            logger.error(
                LogCategory.LOCATION,
                "GeofenceBroadcastReceiver: Geofencing error occurred: $errorCode",
            )
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                logger.info(LogCategory.LOCATION, "GeofenceBroadcastReceiver: Entered geofence")
                geofencingEvent.triggeringGeofences?.forEach { geofence ->
                    val stopId = geofence.requestId.toLongOrNull()
                    if (stopId != null) {
                        // TODO: Получить stopType из кеша или передать через Intent
                        // Пока используем дефолтное значение
                        val stopType = 0
                        scope.launch {
                            GeofenceEventBus.flow.emit(
                                GeofenceEvent.Entered(
                                    stopId = stopId,
                                    stopType = stopType,
                                ),
                            )
                        }
                    }
                }
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                logger.info(LogCategory.LOCATION, "GeofenceBroadcastReceiver: Exited geofence")
                geofencingEvent.triggeringGeofences?.forEach { geofence ->
                    val stopId = geofence.requestId.toLongOrNull()
                    if (stopId != null) {
                        // TODO: Получить stopType из кеша или передать через Intent
                        // Пока используем дефолтное значение
                        val stopType = 0
                        scope.launch {
                            GeofenceEventBus.flow.emit(
                                GeofenceEvent.Exited(
                                    stopId = stopId,
                                    stopType = stopType,
                                ),
                            )
                        }
                    }
                }
            }
            else -> {
                logger.warn(
                    LogCategory.LOCATION,
                    "GeofenceBroadcastReceiver: Unknown geofence transition: $geofenceTransition",
                )
            }
        }
    }
}

/**
 * Event bus для передачи событий геозон в GeofenceClient
 */
internal object GeofenceEventBus {
    val flow = kotlinx.coroutines.flow.MutableSharedFlow<GeofenceEvent>(replay = 0)
}

