package com.tracker

import com.tracker.domain.datasource.LocationManager
import com.tracker.domain.model.Location
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.datetime.Instant
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * iOS Location Manager для получения GPS координат в фоне
 * Реализация согласно официальной документации Apple
 */
@OptIn(ExperimentalForeignApi::class)
class IOSLocationManager : LocationManager {
    
    private val _locationFlow = MutableSharedFlow<Location>()
    private var isTracking = false
    private val locationManager = CLLocationManager()
    private val delegate = LocationDelegate()
    
    init {
        // Настраиваем delegate
        locationManager.delegate = delegate
        
        // Настраиваем параметры согласно документации Apple
        locationManager.desiredAccuracy = platform.CoreLocation.kCLLocationAccuracyBest
        locationManager.distanceFilter = 10.0 // 10 метров
        
        // Включаем фоновые обновления (требует Always разрешение)
        locationManager.allowsBackgroundLocationUpdates = true
        locationManager.pausesLocationUpdatesAutomatically = false
        
        // Настраиваем callback для получения координат
        delegate.onLocationUpdate = { location ->
            if (isTracking) {
                _locationFlow.tryEmit(location)
            }
        }
        
        // Настраиваем callback для изменений разрешений
        delegate.onAuthorizationChange = { status ->
            when (status) {
                kCLAuthorizationStatusAuthorizedAlways -> {
                    println("IOSLocationManager: Always authorization granted, starting tracking")
                    startActualTracking()
                }
                kCLAuthorizationStatusAuthorizedWhenInUse -> {
                    println("IOSLocationManager: When-in-use authorization granted")
                }
                else -> {
                    println("IOSLocationManager: Location authorization denied or restricted")
                }
            }
        }
    }
    
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
                        startActualTracking()
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
            locationManager.stopUpdatingLocation()
            println("IOSLocationManager: Location tracking stopped")
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
     * Запускает реальный GPS трекинг
     */
    private fun startActualTracking() {
        isTracking = true
        locationManager.startUpdatingLocation()
        println("IOSLocationManager: Real GPS tracking started")
    }
    
    /**
     * Delegate для CLLocationManager согласно документации Apple
     */
    private class LocationDelegate : NSObject(), CLLocationManagerDelegateProtocol {
        var onLocationUpdate: ((Location) -> Unit)? = null
        var onAuthorizationChange: ((Int) -> Unit)? = null
        
        /**
         * Получена новая GPS координата
         */
        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            val locations = didUpdateLocations as List<CLLocation>
            val latestLocation = locations.lastOrNull() ?: return
            
            val domainLocation = Location(
                latitude = latestLocation.coordinate.latitude,
                longitude = latestLocation.coordinate.longitude,
                accuracy = latestLocation.horizontalAccuracy.toFloat(),
                altitude = latestLocation.altitude,
                speed = latestLocation.speed.toFloat(),
                bearing = latestLocation.course.toFloat(),
                timestamp = Instant.fromEpochMilliseconds((latestLocation.timestamp.timeIntervalSince1970 * 1000).toLong()),
                deviceId = "40329715"
            )
            
            println("IOSLocationManager: Real GPS Location received")
            println("IOSLocationManager: Lat: ${domainLocation.latitude}, Lon: ${domainLocation.longitude}")
            println("IOSLocationManager: Accuracy: ${domainLocation.accuracy}m, Speed: ${domainLocation.speed}m/s")
            
            onLocationUpdate?.invoke(domainLocation)
        }
        
        /**
         * Ошибка получения GPS
         */
        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            println("IOSLocationManager: GPS error: ${didFailWithError.localizedDescription}")
        }
        
        /**
         * Изменение статуса разрешений
         */
        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            val status = manager.authorizationStatus
            println("IOSLocationManager: Authorization status changed: $status")
            onAuthorizationChange?.invoke(status.toInt())
        }
    }
}