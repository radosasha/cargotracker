package com.tracker.data.datasource.impl

import com.tracker.data.datasource.GpsManager
import com.tracker.data.model.GpsLocation
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSError
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * iOS реализация GpsManager
 * Прямая реализация GPS трекинга для iOS (аналогично AndroidGpsManager)
 */
@OptIn(ExperimentalForeignApi::class)
class IOSGpsManager : GpsManager {

    private val gpsLocationFlow = MutableSharedFlow<GpsLocation>(replay = 1)
    private val locationManager = CLLocationManager()
    private val delegate = LocationDelegate()
    private var isTracking = false

    // Coroutine scope for emitting to flow
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        // Настраиваем delegate
        locationManager.delegate = delegate

        // Настраиваем параметры согласно документации Apple
        locationManager.desiredAccuracy = platform.CoreLocation.kCLLocationAccuracyBest
        locationManager.distanceFilter = 200.0 // 200 метров

        // Включаем фоновые обновления (требует Always разрешение)
        locationManager.allowsBackgroundLocationUpdates = true
        locationManager.pausesLocationUpdatesAutomatically = false

        // Настраиваем callback для получения координат
        delegate.onLocationUpdate = { location ->
            println("IOSGpsManager: onNewLocation callback called")
            println("IOSGpsManager: Emitting location to flow")

            // Всегда используем корутину для emit, чтобы гарантировать доставку
            scope.launch {
                val gpsLocation = convertToGpsLocation(location)
                gpsLocationFlow.emit(gpsLocation)
                println("IOSGpsManager: Successfully emitted GPS location to flow")
            }

            println("IOSGpsManager: Flow has collectors: ${gpsLocationFlow.subscriptionCount.value}")
        }

        // Настраиваем callback для изменений разрешений (только для логирования)
        delegate.onAuthorizationChange = { status ->
            when (status) {
                kCLAuthorizationStatusAuthorizedAlways -> {
                    println("IOSGpsManager: ✅ Authorization: Always")
                }
                kCLAuthorizationStatusAuthorizedWhenInUse -> {
                    println("IOSGpsManager: ⚠️ Authorization: When In Use (may not work in background)")
                }
                kCLAuthorizationStatusDenied -> {
                    println("IOSGpsManager: ❌ Authorization: Denied")
                }
                kCLAuthorizationStatusNotDetermined -> {
                    println("IOSGpsManager: ❓ Authorization: Not Determined")
                }
                kCLAuthorizationStatusRestricted -> {
                    println("IOSGpsManager: 🚫 Authorization: Restricted")
                }
                else -> {
                    println("IOSGpsManager: ❓ Authorization: Unknown status $status")
                }
            }
        }
    }

    override suspend fun startGpsTracking(): Result<Unit> {
        println("IOSGpsManager: startGpsTracking() called")
        return try {
            // Запускаем трекинг
            println("IOSGpsManager: Starting location tracking")

            dispatch_async(dispatch_get_main_queue()) {
                startActualTracking()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            println("IOSGpsManager: Error starting GPS tracking: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun stopGpsTracking(): Result<Unit> {
        println("IOSGpsManager: stopGpsTracking() called")
        return try {
            // Останавливаем трекинг
            dispatch_async(dispatch_get_main_queue()) {
                locationManager.stopUpdatingLocation()
                println("IOSGpsManager: Real GPS tracking stopped")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            println("IOSGpsManager: Error stopping GPS tracking: ${e.message}")
            Result.failure(e)
        }
    }

    override fun isGpsTrackingActive(): Boolean {
        // Проверяем статус через locationManager
        val isActive = locationManager.location != null
        println("IOSGpsManager: isGpsTrackingActive() = $isActive")
        return isActive
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

    /**
     * Запускает реальный GPS трекинг
     */
    private fun startActualTracking() {
        // Трекинг активен
        locationManager.startUpdatingLocation()
        println("IOSGpsManager: Real GPS tracking started")
    }

    /**
     * Конвертирует CLLocation в GpsLocation
     */
    private fun convertToGpsLocation(location: CLLocation): GpsLocation {
        return location.coordinate.useContents {
            GpsLocation(
                latitude = latitude,
                longitude = longitude,
                accuracy = location.horizontalAccuracy.toFloat(),
                altitude = location.altitude,
                speed = location.speed.toFloat(),
                bearing = location.course.toFloat(),
                timestamp = Clock.System.now(),
                provider = "ios"
            )
        }
    }
}

/**
 * Delegate для CLLocationManager
 */
@OptIn(ExperimentalForeignApi::class)
class LocationDelegate : NSObject(), CLLocationManagerDelegateProtocol {

    var onLocationUpdate: ((CLLocation) -> Unit)? = null
    var onAuthorizationChange: ((Int) -> Unit)? = null

    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        println("LocationDelegate: didUpdateLocations called with ${didUpdateLocations.size} locations")

        if (didUpdateLocations.isNotEmpty()) {
            val location = didUpdateLocations.last() as CLLocation
            println("LocationDelegate: Latest location received")

            onLocationUpdate?.invoke(location)
        }
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        println("LocationDelegate: didFailWithError: ${didFailWithError.localizedDescription}")
    }

    override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: Int) {
        println("LocationDelegate: didChangeAuthorizationStatus: $didChangeAuthorizationStatus")
        onAuthorizationChange?.invoke(didChangeAuthorizationStatus)
    }
}
