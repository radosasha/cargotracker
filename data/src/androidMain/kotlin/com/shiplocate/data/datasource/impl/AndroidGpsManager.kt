package com.shiplocate.data.datasource.impl

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val gpsLocationFlow = MutableSharedFlow<GpsLocation>(replay = 0, extraBufferCapacity = 5)
    private var isTracking = false

    // samsung fallback
    private val isSamsungDevice: Boolean =
        Build.MANUFACTURER?.lowercase()?.contains("samsung") == true
    private var fallbackJob: Job? = null
    private var fallbackListener: LocationListener? = null
    private var isFallbackActive: Boolean = false
    private var hasReceivedFusedLocation: Boolean = false
    private val locationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val FALLBACK_TIMEOUT_MS = 5 * 60_000L

    // Coroutine scope for emitting to flow
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        // Интервалы обновления GPS (в миллисекундах)
        const val INTERVAL_MS = 60 * 1000L // 1 минута
        const val MIN_UPDATE_MS = 40 * 1000L // 1 минута
        const val MIN_DISTANCE_M = 400f // 20 метров
    }

    @SuppressLint("MissingPermission")
    override suspend fun startGpsTracking(): Flow<GpsLocation> {
        if (isTracking) {
            logger.info(LogCategory.LOCATION, "AndroidGpsManager: Already tracking")
            return gpsLocationFlow
        }

        try {
            logger.info(LogCategory.LOCATION, "AndroidGpsManager: Starting GPS tracking with FusedLocationProviderClient")

            hasReceivedFusedLocation = false
            if (isSamsungDevice) {
                startInitialFallbackTimer()
            }
            // Создаем LocationRequest с настройками
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                INTERVAL_MS,
            )
                .setMinUpdateDistanceMeters(MIN_DISTANCE_M)
                .setMaxUpdateDelayMillis(INTERVAL_MS)
                .setMinUpdateIntervalMillis(MIN_UPDATE_MS)
                .build()

            suspendCoroutine { cont ->
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
            stopInitialFallbackTimer()
            stopLocationManagerFallback()
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
                hasReceivedFusedLocation = true
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
                        stopInitialFallbackTimer()
                        stopLocationManagerFallback()
                        hasReceivedFusedLocation = true
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

    private fun startInitialFallbackTimer() {
        if (fallbackJob?.isActive == true) return

        fallbackJob = scope.launch {
            logger.info(
                LogCategory.LOCATION,
                "AndroidGpsManager: Starting Samsung initial fallback timer",
            )
            delay(FALLBACK_TIMEOUT_MS)
            if (!hasReceivedFusedLocation && isTracking) {
                logger.warn(
                    LogCategory.LOCATION,
                    "AndroidGpsManager: No fused location received after start, activating LocationManager fallback",
                )
                startLocationManagerFallback()
            }
        }
    }

    private fun stopInitialFallbackTimer() {
        fallbackJob?.cancel()
        fallbackJob = null
    }

    @SuppressLint("MissingPermission")
    private fun startLocationManagerFallback() {
        if (isFallbackActive) return
        val manager = locationManager ?: run {
            logger.error(LogCategory.LOCATION, "AndroidGpsManager: LocationManager not available, cannot start fallback")
            return
        }
        logger.warn(
            LogCategory.LOCATION,
            "AndroidGpsManager: Activating LocationManager fallback, stopping fused provider",
        )
        fusedLocationClient.removeLocationUpdates(locationCallback)

        val listener = object : LocationListener {

            override fun onLocationChanged(location: AndroidLocation) {
                logger.info(LogCategory.LOCATION, "AndroidGpsManager: Fallback location received")
                val gpsLocation = convertToGpsLocation(location.apply { provider = "network" })
                scope.launch {
                    gpsLocationFlow.emit(gpsLocation)
                }
            }

            override fun onFlushComplete(requestCode: Int) {
                logger.info(LogCategory.LOCATION, "onFlushComplete: requestCode:${requestCode}")
            }

            override fun onProviderDisabled(provider: String) {
                logger.info(LogCategory.LOCATION, "onProviderDisabled: provider:${provider}")
            }

            override fun onProviderEnabled(provider: String) {
                logger.info(LogCategory.LOCATION, "onProviderEnabled: provider:${provider}")
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                logger.info(LogCategory.LOCATION, "onStatusChanged: provider:${provider}, status:${status}, extras:${extras}")
            }

            override fun onLocationChanged(locations: List<AndroidLocation?>) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    super.onLocationChanged(locations)
                }
                locations.forEach { location ->
                    if (location != null) {
                        val gpsLocation = convertToGpsLocation(location.apply { provider = "network" })
                        scope.launch {
                            gpsLocationFlow.emit(gpsLocation)
                        }
                    }
                }
            }
        }

        try {
            manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_UPDATE_MS,
                MIN_DISTANCE_M,
                listener,
                Looper.getMainLooper(),
            )
            manager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_UPDATE_MS,
                MIN_DISTANCE_M,
                listener,
                Looper.getMainLooper(),
            )
            fallbackListener = listener
            isFallbackActive = true
            logger.info(LogCategory.LOCATION, "AndroidGpsManager: LocationManager fallback started")
        } catch (e: SecurityException) {
            logger.error(LogCategory.LOCATION, "AndroidGpsManager: Failed to start fallback due to permissions: ${e.message}")
        } catch (e: Exception) {
            logger.error(LogCategory.LOCATION, "AndroidGpsManager: Failed to start fallback: ${e.message}")
        }
    }

    private fun stopLocationManagerFallback() {
        logger.info(LogCategory.LOCATION, "AndroidGpsManager: Trying to stop Samsung fallback")
        if (!isFallbackActive) return
        val manager = locationManager ?: return
        fallbackListener?.let {
            manager.removeUpdates(it)
        }
        fallbackListener = null
        isFallbackActive = false
        logger.info(LogCategory.LOCATION, "AndroidGpsManager: LocationManager fallback stopped")
    }
}
