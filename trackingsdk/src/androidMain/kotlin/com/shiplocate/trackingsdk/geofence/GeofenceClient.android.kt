package com.shiplocate.trackingsdk.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.load.Stop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * Android actual реализация GeofenceClient
 * Использует Google Play Services GeofencingClient
 */
actual class GeofenceClient(
    private val context: Context,
    private val logger: Logger,
    private val scope: CoroutineScope,
) {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    companion object {
        private const val TAG = "GeofenceClient"
    }

    /**
     * Добавляет геозону для отслеживания
     */
    @SuppressLint("MissingPermission")
    actual suspend fun addGeofence(stop: Stop) {
        try {
            val geofence = Geofence.Builder()
                .setRequestId(stop.id.toString())
                // FIXME
                .setCircularRegion(
                    45.0355,
                    38.9753,
                    50000f,
                )
//                .setCircularRegion(
//                    stop.latitude,
//                    stop.longitude,
//                    stop.geofenceRadius.toFloat(),
//                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            val pendingIntent = createPendingIntent()

            val task: Task<Void> = geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            task.addOnSuccessListener {
                logger.info(
                    LogCategory.LOCATION,
                    "$TAG: Geofence added successfully for stop ${stop.id} (type: ${stop.type})",
                )
            }.addOnFailureListener { e ->
                val errorMessage = when {
                    e is ResolvableApiException -> {
                        "Resolvable error: ${e.message}"
                    }

                    else -> {
                        val errorCode = GeofenceStatusCodes.getStatusCodeString(
                            (e as? com.google.android.gms.common.api.ApiException)?.statusCode
                                ?: GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE,
                        )
                        "Error code: $errorCode, Message: ${e.message}"
                    }
                }
                logger.error(
                    LogCategory.LOCATION,
                    "$TAG: Failed to add geofence for stop ${stop.id}: $errorMessage",
                    e,
                )
            }
        } catch (e: Exception) {
            logger.error(
                LogCategory.LOCATION,
                "$TAG: Error adding geofence for stop ${stop.id}: ${e.message}",
                e,
            )
        }
    }

    /**
     * Удаляет геозону
     */
    actual suspend fun removeGeofence(stopId: Long) {
        try {
            val task: Task<Void> = geofencingClient.removeGeofences(listOf(stopId.toString()))
            task.addOnSuccessListener {
                logger.info(LogCategory.LOCATION, "$TAG: Geofence removed for stop $stopId")
            }.addOnFailureListener { e ->
                logger.error(
                    LogCategory.LOCATION,
                    "$TAG: Error removing geofence for stop $stopId: ${e.message}",
                    e,
                )
            }
        } catch (e: Exception) {
            logger.error(
                LogCategory.LOCATION,
                "$TAG: Error removing geofence for stop $stopId: ${e.message}",
                e,
            )
        }
    }

    /**
     * Удаляет все геозоны
     */
    actual suspend fun removeAllGeofences() {
        try {
            val pendingIntent = createPendingIntent()
            val task: Task<Void> = geofencingClient.removeGeofences(pendingIntent)
            task.addOnSuccessListener {
                logger.info(LogCategory.LOCATION, "$TAG: All geofences removed")
            }.addOnFailureListener { e ->
                logger.error(
                    LogCategory.LOCATION,
                    "$TAG: Error removing all geofences: ${e.message}",
                    e,
                )
            }
        } catch (e: Exception) {
            logger.error(
                LogCategory.LOCATION,
                "$TAG: Error removing all geofences: ${e.message}",
                e,
            )
        }
    }

    /**
     * Поток событий геозон
     */
    actual fun observeGeofenceEvents(): Flow<GeofenceEvent> {
        return GeofenceEventBus.flow
    }

    /**
     * Очищает ресурсы
     */
    actual suspend fun destroy() {
        logger.info(LogCategory.LOCATION, "$TAG: Destroying GeofenceClient")
        // GeofencingClient не требует явного уничтожения
    }

    /**
     * Создает PendingIntent для получения событий геозон
     */
    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = "com.shiplocate.trackingsdk.GEOFENCE_TRANSITION"
        }

        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Для геозон НУЖНО использовать FLAG_MUTABLE, чтобы Google Play Services
                // мог добавить данные о геозоне в Intent
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

        return PendingIntent.getBroadcast(context, 0, intent, flags)
    }
}

