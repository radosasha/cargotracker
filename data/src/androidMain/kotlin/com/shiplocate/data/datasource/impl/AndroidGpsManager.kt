package com.shiplocate.data.datasource.impl

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.GpsManager
import com.shiplocate.data.model.GpsLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import android.location.Location as AndroidLocation

/**
 * Android-специфичная реализация GpsManager
 * Отвечает за получение GPS координат через FusedLocationProviderClient
 */
class AndroidGpsManager(
    private val context: Context,
    private val logger: Logger,
) : GpsManager {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // TODO replay 0, onBufferOverflow
    private val gpsLocationFlow = MutableSharedFlow<GpsLocation>(replay = 1, extraBufferCapacity = 5)
    private var isTracking = false

    // Coroutine scope for emitting to flow
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        // Интервалы обновления GPS (в миллисекундах)
        private const val INTERVAL_MS = 60 * 1000L // 1 минута
        private const val MIN_UPDATE_MS = 55 * 1000L // 1 минута
        private const val MIN_DISTANCE_M = 400f // 20 метров
    }

    @SuppressLint("MissingPermission")
    override suspend fun startGpsTracking(): Flow<GpsLocation> {
        if (isTracking) {
            logger.info(LogCategory.LOCATION, "AndroidGpsManager: Already tracking")
            return gpsLocationFlow
        }

        // Проверяем разрешения
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            logger.info(LogCategory.LOCATION, "AndroidGpsManager: Location permission not granted")
            throw IllegalStateException("Location permission not granted")
        }

        try {
            logger.info(LogCategory.LOCATION, "AndroidGpsManager: Starting GPS tracking with FusedLocationProviderClient")

            // Создаем LocationRequest с настройками
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                INTERVAL_MS,
            )
                .setMinUpdateDistanceMeters(MIN_DISTANCE_M)
                .setMaxUpdateDelayMillis(INTERVAL_MS)
                .setMinUpdateIntervalMillis(MIN_UPDATE_MS)
                .build()

            suspendCoroutine<Unit> { cont ->
                // Запрашиваем обновления местоположения
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper(),
                ).addOnSuccessListener {
                    logger.info(LogCategory.LOCATION, "AndroidGpsManager: FusedProvider registered successfully successfully")
                    cont.resumeWith(Result.success(Unit))
                }.addOnFailureListener { it ->
                    logger.info(LogCategory.LOCATION, "AndroidGpsManager: failed to start FusedProvider: ${it}")
                    cont.resumeWithException(it)
                }
            }

            isTracking = true
            logger.info(LogCategory.LOCATION, "AndroidGpsManager: GPS tracking started successfully")
            return gpsLocationFlow
        } catch (e: SecurityException) {
            logger.error(LogCategory.LOCATION, "AndroidGpsManager: Security exception: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error(LogCategory.LOCATION, "AndroidGpsManager: Error starting tracking: ${e.message}")
            throw e
        }
    }

    override suspend fun stopGpsTracking(): Result<Unit> {
        if (!isTracking) {
            logger.info(LogCategory.LOCATION, "AndroidGpsManager: Not tracking")
            return Result.success(Unit)
        }

        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            isTracking = false
            logger.info(LogCategory.LOCATION, "AndroidGpsManager: GPS tracking stopped")
            return Result.success(Unit)
        } catch (e: Exception) {
            logger.error(LogCategory.LOCATION, "AndroidGpsManager: Error stopping tracking: ${e.message}")
            return Result.failure(e)
        }
    }

    override fun isGpsTrackingActive(): Boolean {
        return isTracking
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locations = locationResult.locations
            if (locations.isEmpty()) {
                logger.error(LogCategory.LOCATION, "AndroidGpsManager: Received empty list of locations")
            } else {
                locations.forEach { androidLocation ->
                    logger.info(LogCategory.LOCATION, "AndroidGpsManager: GPS Location received")
                    logger.info(
                        LogCategory.LOCATION,
                        "AndroidGpsManager: Lat: ${androidLocation.latitude}, Lon: ${androidLocation.longitude}"
                    )
                    logger.info(
                        LogCategory.LOCATION,
                        "AndroidGpsManager: Accuracy: ${androidLocation.accuracy}m, Time: ${androidLocation.time}"
                    )
                    logger.info(
                        LogCategory.LOCATION,
                        "AndroidGpsManager: Speed: ${if (androidLocation.hasSpeed()) androidLocation.speed else "N/A"} m/s"
                    )
                    logger.info(
                        LogCategory.LOCATION,
                        "AndroidGpsManager: Bearing: ${if (androidLocation.hasBearing()) androidLocation.bearing else "N/A"}°"
                    )

                    // Конвертируем Android Location в GpsLocation
                    val gpsLocation = convertToGpsLocation(androidLocation)

                    // Эмитим в flow
                    scope.launch {
                        gpsLocationFlow.emit(gpsLocation)
                        logger.info(LogCategory.LOCATION, "AndroidGpsManager: Location emitted to flow")
                    }
                }
            }
        }

        override fun onLocationAvailability(p0: LocationAvailability) {
            super.onLocationAvailability(p0)
            logger.info(
                LogCategory.LOCATION,
                "AndroidGpsManager: Location availability changed, isAvailable: ${p0.isLocationAvailable}"
            )

        }
    }

    private fun convertToGpsLocation(androidLocation: AndroidLocation): GpsLocation {
        return GpsLocation(
            latitude = androidLocation.latitude,
            longitude = androidLocation.longitude,
            accuracy = androidLocation.accuracy,
            altitude = if (androidLocation.hasAltitude()) androidLocation.altitude else null,
            speed = if (androidLocation.hasSpeed()) androidLocation.speed else null,
            bearing = if (androidLocation.hasBearing()) androidLocation.bearing else null,
            timestamp = Instant.fromEpochMilliseconds(androidLocation.time),
            provider = androidLocation.provider ?: "fused",
        )
    }
}
