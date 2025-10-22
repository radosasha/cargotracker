package com.shiplocate.data.datasource.impl

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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onStart
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
        private const val MIN_DISTANCE_M = 500f // 500 метров
    }

    override suspend fun startGpsTracking(): Result<Unit> {
        if (isTracking) {
            logger.debug(LogCategory.LOCATION, "AndroidGpsManager: Already tracking")
            return Result.success(Unit)
        }

        // Проверяем разрешения
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            logger.debug(LogCategory.LOCATION, "AndroidGpsManager: Location permission not granted")
            return Result.failure(SecurityException("Location permission not granted"))
        }

        try {
            logger.debug(LogCategory.LOCATION, "AndroidGpsManager: Starting GPS tracking")

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
            logger.debug(LogCategory.LOCATION, "AndroidGpsManager: GPS tracking started successfully")
            return Result.success(Unit)
        } catch (e: SecurityException) {
            logger.debug(LogCategory.LOCATION, "AndroidGpsManager: Security exception: ${e.message}")
            return Result.failure(e)
        } catch (e: Exception) {
            logger.debug(LogCategory.LOCATION, "AndroidGpsManager: Error starting tracking: ${e.message}")
            return Result.failure(e)
        }
    }

    override suspend fun stopGpsTracking(): Result<Unit> {
        if (!isTracking) {
            logger.debug(LogCategory.LOCATION, "AndroidGpsManager: Not tracking")
            return Result.success(Unit)
        }

        try {
            androidLocationManager.removeUpdates(locationListener)
            isTracking = false
            logger.debug(LogCategory.LOCATION, "AndroidGpsManager: GPS tracking stopped")
            return Result.success(Unit)
        } catch (e: Exception) {
            logger.debug(LogCategory.LOCATION, "AndroidGpsManager: Error stopping tracking: ${e.message}")
            return Result.failure(e)
        }
    }

    override fun isGpsTrackingActive(): Boolean {
        return isTracking
    }

    override fun observeGpsLocations(): Flow<GpsLocation> {
        return gpsLocationFlow.asSharedFlow()
            .onStart {
                // Автоматически запускаем GPS трекинг при подписке
                if (!isTracking) {
                    startGpsTracking()
                }
            }
    }

    private val locationListener =
        LocationListener { androidLocation ->
            logger.debug(LogCategory.LOCATION, "AndroidGpsManager: GPS Location received")
            logger.debug(LogCategory.LOCATION, "AndroidGpsManager: Lat: ${androidLocation.latitude}, Lon: ${androidLocation.longitude}")
            logger.debug(LogCategory.LOCATION, "AndroidGpsManager: Accuracy: ${androidLocation.accuracy}m, Time: ${androidLocation.time}")
            logger.debug(LogCategory.LOCATION, "AndroidGpsManager: Speed: ${if (androidLocation.hasSpeed()) androidLocation.speed else "N/A"} m/s")
            logger.debug(LogCategory.LOCATION, "AndroidGpsManager: Bearing: ${if (androidLocation.hasBearing()) androidLocation.bearing else "N/A"}°")

            // Конвертируем Android Location в GpsLocation
            val gpsLocation = convertToGpsLocation(androidLocation)

            // Эмитим в flow
            scope.launch {
                gpsLocationFlow.emit(gpsLocation)
                logger.debug(LogCategory.LOCATION, "AndroidGpsManager: Location emitted to flow")
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
