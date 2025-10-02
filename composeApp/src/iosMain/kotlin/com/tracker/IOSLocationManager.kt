package com.tracker

import com.tracker.domain.datasource.LocationManager
import com.tracker.domain.model.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.datetime.Clock
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * iOS Location Manager для получения GPS координат
 * Упрощенная версия для демонстрации архитектуры
 */
class IOSLocationManager : LocationManager {
    
    private val _locationFlow = MutableSharedFlow<Location>()
    private var isTracking = false
    private val locationManager = CLLocationManager()
    
    /**
     * Запускает GPS трекинг
     */
    override fun startLocationTracking(): Result<Unit> {
        return try {
            dispatch_async(dispatch_get_main_queue()) {
                when (locationManager.authorizationStatus) {
                    kCLAuthorizationStatusNotDetermined -> {
                        println("IOSLocationManager: Requesting location permission")
                        locationManager.requestWhenInUseAuthorization()
                    }
                    kCLAuthorizationStatusAuthorizedWhenInUse -> {
                        println("IOSLocationManager: Requesting background location permission")
                        locationManager.requestAlwaysAuthorization()
                    }
                    kCLAuthorizationStatusAuthorizedAlways -> {
                        println("IOSLocationManager: Location permission granted, starting tracking")
                        isTracking = true
                    }
                    else -> {
                        println("IOSLocationManager: Location permission denied or restricted")
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Останавливает GPS трекинг
     */
    override fun stopLocationTracking(): Result<Unit> {
        return try {
            isTracking = false
            println("IOSLocationManager: Location tracking stopped (simulated)")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Проверяет, активен ли трекинг
     */
    override fun isLocationTrackingActive(): Boolean = isTracking
    
    /**
     * Наблюдает за GPS координатами
     */
    override fun observeLocationUpdates(): Flow<Location> = _locationFlow.asSharedFlow()
    
    /**
     * Симулирует получение GPS координаты (для демонстрации)
     */
    fun simulateLocationUpdate() {
        if (isTracking) {
            val simulatedLocation = Location(
                latitude = 55.7558, // Москва
                longitude = 37.6176,
                accuracy = 10f,
                altitude = 150.0,
                speed = 5.0f,
                bearing = 90f,
                timestamp = Clock.System.now(),
                deviceId = "40329715"
            )
            
            println("IOSLocationManager: Simulated GPS Location received")
            println("IOSLocationManager: Lat: ${simulatedLocation.latitude}, Lon: ${simulatedLocation.longitude}")
            
            _locationFlow.tryEmit(simulatedLocation)
        }
    }
}
