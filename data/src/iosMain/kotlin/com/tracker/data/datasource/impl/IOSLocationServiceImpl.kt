package com.tracker.data.datasource.impl

import com.tracker.domain.datasource.IOSLocationService
import com.tracker.domain.model.Location
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
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
class IOSLocationServiceImpl : IOSLocationService {

    private val _locationFlow = MutableSharedFlow<Location>(replay = 1)
    private var isTracking = false
    private val locationManager = CLLocationManager()
    private val delegate = LocationDelegate()
    
    // Coroutine scope for emitting to flow
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        // Настраиваем delegate
        locationManager.delegate = delegate

        // Настраиваем параметры согласно документации Apple
        locationManager.desiredAccuracy = platform.CoreLocation.kCLLocationAccuracyBest
        locationManager.distanceFilter = 200.0 // 10 метров

        // Включаем фоновые обновления (требует Always разрешение)
        locationManager.allowsBackgroundLocationUpdates = true
        locationManager.pausesLocationUpdatesAutomatically = false

        // Настраиваем callback для получения координат
        delegate.onLocationUpdate = { location ->
            println("IOSLocationManager: onNewLocation callback called")
            if (isTracking) {
                println("IOSLocationManager: isTracking = true, emitting to flow")
                
                // Всегда используем корутину для emit, чтобы гарантировать доставку
                scope.launch {
                    _locationFlow.emit(location)
                    println("IOSLocationManager: Successfully emitted location to flow")
                }
                
                println("IOSLocationManager: Flow has collectors: ${_locationFlow.subscriptionCount.value}")
            } else {
                println("IOSLocationManager: isTracking = false, not emitting")
            }
        }

        // Настраиваем callback для изменений разрешений (только для логирования)
        delegate.onAuthorizationChange = { status ->
            when (status) {
                kCLAuthorizationStatusAuthorizedAlways -> {
                    println("IOSLocationManager: Always authorization granted")
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
     * Предполагается, что разрешения уже проверены и получены
     */
    override fun startLocationTracking(): Result<Unit> {
        println("IOSLocationManager: startLocationTracking() called")
        println("IOSLocationManager: Instance: ${this.hashCode()}")
        return try {
            // Устанавливаем isTracking = true и запускаем трекинг
            isTracking = true
            println("IOSLocationManager: isTracking set to true")
            
            dispatch_async(dispatch_get_main_queue()) {
                startActualTracking()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            isTracking = false
            println("IOSLocationManager: Error starting tracking: ${e.message}")
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
    override fun isLocationTrackingActive(): Boolean {
        println("IOSLocationManager: isLocationTrackingActive() = $isTracking")
        return isTracking
    }

    /**
     * Наблюдает за GPS координатами
     */
    override fun observeLocationUpdates(): Flow<Location> {
        println("IOSLocationManager: observeLocationUpdates() called")
        return _locationFlow.asSharedFlow()
    }

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
                latitude = latestLocation.coordinate().useContents { latitude },
                longitude = latestLocation.coordinate().useContents { longitude },
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
            println("IOSLocationManager: onLocationUpdate callback: ${onLocationUpdate != null}")

            onLocationUpdate?.invoke(domainLocation)
        }

        /**
         * Ошибка получения GPS
         */
        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            val errorCode = didFailWithError.code.toInt()
            when (errorCode) {
                0 -> {
                    // kCLErrorLocationUnknown - нормальная ошибка в симуляторе
                    println("IOSLocationManager: GPS temporarily unavailable (normal in simulator)")
                }
                1 -> {
                    // kCLErrorDenied - разрешение отклонено
                    println("IOSLocationManager: GPS access denied")
                }
                else -> {
                    println("IOSLocationManager: GPS error: ${didFailWithError.localizedDescription}")
                }
            }
        }

        /**
         * Изменение статуса разрешений
         */
        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            val status = manager.authorizationStatus
            println("IOSLocationManager: Authorization status changed: $status")
            when (manager.authorizationStatus) {
                kCLAuthorizationStatusAuthorizedAlways -> {
                    println("✅ Разрешено всегда")
                }

                kCLAuthorizationStatusAuthorizedWhenInUse -> {
                    println("✅ Разрешено только при использовании")
                }

                kCLAuthorizationStatusDenied -> {
                    println("❌ Доступ запрещён пользователем")
                }

                kCLAuthorizationStatusRestricted -> {
                    println("⚠️ Доступ ограничен (например, родительский контроль)")
                }

                kCLAuthorizationStatusNotDetermined -> {
                    print("🤔 Пользователь ещё не выбрал")
                }

                else -> {
                    println("Неизвестно")
                }
            }
            onAuthorizationChange?.invoke(status)
        }
    }
}
