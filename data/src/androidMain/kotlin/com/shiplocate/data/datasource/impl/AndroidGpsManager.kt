package com.shiplocate.data.datasource.impl

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.os.Looper
import androidx.core.app.ActivityCompat
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
import android.location.Location as AndroidLocation
import android.location.LocationManager as AndroidLocationManager

/**
 * Android-специфичная реализация GpsManager
 * Отвечает за получение GPS координат через Android LocationManager
 */
class AndroidGpsManager(
    private val context: Context,
    private val logger: Logger,
) : GpsManager {
    private val androidLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager
    private val gpsLocationFlow = MutableSharedFlow<GpsLocation>(replay = 1)
    private var isTracking = false

    // Coroutine scope for emitting to flow
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        // Интервалы обновления GPS (в миллисекундах)
        private const val MIN_TIME_MS = 60 * 1000L // 1 минута
        private const val MIN_DISTANCE_M = 200f // 200 метров
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
            logger.info(LogCategory.LOCATION, "AndroidGpsManager: Starting GPS tracking")

            // Запрашиваем обновления GPS
            androidLocationManager.requestLocationUpdates(
                AndroidLocationManager.GPS_PROVIDER,
                MIN_TIME_MS,
                MIN_DISTANCE_M,
                locationListener,
                Looper.getMainLooper(),
            )

            // Также используем Network Provider как резерв
            androidLocationManager.requestLocationUpdates(
                AndroidLocationManager.NETWORK_PROVIDER,
                MIN_TIME_MS,
                MIN_DISTANCE_M,
                locationListener,
                Looper.getMainLooper(),
            )

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
            androidLocationManager.removeUpdates(locationListener)
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

    private val locationListener =
        LocationListener { androidLocation ->
            logger.info(LogCategory.LOCATION, "AndroidGpsManager: GPS Location received")
            logger.info(LogCategory.LOCATION, "AndroidGpsManager: Lat: ${androidLocation.latitude}, Lon: ${androidLocation.longitude}")
            logger.info(LogCategory.LOCATION, "AndroidGpsManager: Accuracy: ${androidLocation.accuracy}m, Time: ${androidLocation.time}")
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

    private fun convertToGpsLocation(androidLocation: AndroidLocation): GpsLocation {
        return GpsLocation(
            latitude = androidLocation.latitude,
            longitude = androidLocation.longitude,
            accuracy = androidLocation.accuracy,
            altitude = if (androidLocation.hasAltitude()) androidLocation.altitude else null,
            speed = if (androidLocation.hasSpeed()) androidLocation.speed else null,
            bearing = if (androidLocation.hasBearing()) androidLocation.bearing else null,
            timestamp = Instant.fromEpochMilliseconds(androidLocation.time),
            provider = androidLocation.provider ?: "unknown",
        )
    }
}
