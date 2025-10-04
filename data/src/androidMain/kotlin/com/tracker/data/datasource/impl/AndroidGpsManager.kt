package com.tracker.data.datasource.impl

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location as AndroidLocation
import android.location.LocationListener
import android.location.LocationManager as AndroidLocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.tracker.data.datasource.GpsManager
import com.tracker.data.model.GpsLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

/**
 * Android-специфичная реализация GpsManager
 * Отвечает за получение GPS координат через Android LocationManager
 */
class AndroidGpsManager(
    private val context: Context
) : GpsManager {

    private val androidLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager
    private val _gpsLocationFlow = MutableSharedFlow<GpsLocation>(replay = 1)
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
            println("AndroidGpsManager: Already tracking")
            return Result.success(Unit)
        }

        // Проверяем разрешения
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("AndroidGpsManager: Location permission not granted")
            return Result.failure(SecurityException("Location permission not granted"))
        }

        try {
            println("AndroidGpsManager: Starting GPS tracking")
            
            // Запрашиваем обновления GPS
            androidLocationManager.requestLocationUpdates(
                AndroidLocationManager.GPS_PROVIDER,
                MIN_TIME_MS,
                MIN_DISTANCE_M,
                locationListener,
                Looper.getMainLooper()
            )
            
            // Также используем Network Provider как резерв
            androidLocationManager.requestLocationUpdates(
                AndroidLocationManager.NETWORK_PROVIDER,
                MIN_TIME_MS,
                MIN_DISTANCE_M,
                locationListener,
                Looper.getMainLooper()
            )
            
            isTracking = true
            println("AndroidGpsManager: GPS tracking started successfully")
            return Result.success(Unit)
        } catch (e: SecurityException) {
            println("AndroidGpsManager: Security exception: ${e.message}")
            return Result.failure(e)
        } catch (e: Exception) {
            println("AndroidGpsManager: Error starting tracking: ${e.message}")
            return Result.failure(e)
        }
    }

    override suspend fun stopGpsTracking(): Result<Unit> {
        if (!isTracking) {
            println("AndroidGpsManager: Not tracking")
            return Result.success(Unit)
        }

        try {
            androidLocationManager.removeUpdates(locationListener)
            isTracking = false
            println("AndroidGpsManager: GPS tracking stopped")
            return Result.success(Unit)
        } catch (e: Exception) {
            println("AndroidGpsManager: Error stopping tracking: ${e.message}")
            return Result.failure(e)
        }
    }

    override fun isGpsTrackingActive(): Boolean {
        return isTracking
    }

    override fun observeGpsLocations(): Flow<GpsLocation> {
        return _gpsLocationFlow.asSharedFlow()
            .onStart {
                // Автоматически запускаем GPS трекинг при подписке
                if (!isTracking) {
                    startGpsTracking()
                }
            }
    }

    private val locationListener = LocationListener { androidLocation ->
        println("AndroidGpsManager: GPS Location received")
        println("AndroidGpsManager: Lat: ${androidLocation.latitude}, Lon: ${androidLocation.longitude}")
        println("AndroidGpsManager: Accuracy: ${androidLocation.accuracy}m, Time: ${androidLocation.time}")
        println("AndroidGpsManager: Speed: ${if (androidLocation.hasSpeed()) androidLocation.speed else "N/A"} m/s")
        println("AndroidGpsManager: Bearing: ${if (androidLocation.hasBearing()) androidLocation.bearing else "N/A"}°")

        // Конвертируем Android Location в GpsLocation
        val gpsLocation = convertToGpsLocation(androidLocation)

        // Эмитим в flow
        scope.launch {
            _gpsLocationFlow.emit(gpsLocation)
            println("AndroidGpsManager: Location emitted to flow")
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
            provider = androidLocation.provider ?: "unknown"
        )
    }
}

