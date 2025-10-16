package com.tracker

import com.tracker.data.datasource.PermissionChecker
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.suspendCoroutine

/**
 * iOS реализация PermissionChecker
 */
class IOSPermissionCheckerImpl : PermissionChecker {
    private val locationManager = CLLocationManager()
    private val locationDelegate = LocationManagerDelegate()

    init {
        locationManager.delegate = locationDelegate
    }

    // Делегат для отслеживания изменений статуса разрешений
    private class LocationManagerDelegate : NSObject(), CLLocationManagerDelegateProtocol {
        var onAuthorizationChange: ((Int) -> Unit)? = null

        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            val status = manager.authorizationStatus
            println("iOS: Location authorization changed to: $status")
            onAuthorizationChange?.invoke(status.toInt())
        }
    }

    override suspend fun hasLocationPermissions(): Boolean {
        return suspendCoroutine { continuation ->
            dispatch_async(dispatch_get_main_queue()) {
                val status = locationManager.authorizationStatus
                val hasPermission =
                    when (status) {
                        kCLAuthorizationStatusAuthorizedWhenInUse,
                        kCLAuthorizationStatusAuthorizedAlways,
                        -> true
                        kCLAuthorizationStatusNotDetermined,
                        kCLAuthorizationStatusDenied,
                        kCLAuthorizationStatusRestricted,
                        -> false
                        else -> false
                    }
                continuation.resumeWith(Result.success(hasPermission))
            }
        }
    }

    override suspend fun hasBackgroundLocationPermission(): Boolean {
        return suspendCoroutine { continuation ->
            dispatch_async(dispatch_get_main_queue()) {
                val status = locationManager.authorizationStatus
                val hasPermission = status == kCLAuthorizationStatusAuthorizedAlways
                continuation.resumeWith(Result.success(hasPermission))
            }
        }
    }

    override suspend fun hasNotificationPermission(): Boolean {
        return suspendCoroutine { continuation ->
            dispatch_async(dispatch_get_main_queue()) {
                val center = UNUserNotificationCenter.currentNotificationCenter()
                center.getNotificationSettingsWithCompletionHandler { settings ->
                    val hasPermission = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
                    continuation.resumeWith(Result.success(hasPermission))
                }
            }
        }
    }

    override suspend fun hasAllRequiredPermissions(): Boolean {
        val location = hasLocationPermissions()
        val background = hasBackgroundLocationPermission()
        val notifications = hasNotificationPermission()
        return location && background && notifications
    }

    override suspend fun getPermissionStatusMessage(): String {
        val location = hasLocationPermissions()
        val background = hasBackgroundLocationPermission()
        val notifications = hasNotificationPermission()

        val missingPermissions = mutableListOf<String>()

        if (!location) {
            missingPermissions.add("Location access")
        }
        if (!background) {
            missingPermissions.add("Background location")
        }
        if (!notifications) {
            missingPermissions.add("Notifications")
        }

        return if (missingPermissions.isEmpty()) {
            "All permissions granted"
        } else {
            "Missing: ${missingPermissions.joinToString(", ")}"
        }
    }

    override suspend fun openAppSettings(): Result<Unit> {
        return try {
            UIApplication.sharedApplication.openURL(NSURL(string = UIApplicationOpenSettingsURLString))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun requestAllPermissions() {
        // Запрашиваем разрешения последовательно, используя callbacks
        dispatch_async(dispatch_get_main_queue()) {
            val status = locationManager.authorizationStatus
            println("iOS: Current authorization status: $status")

            when (status) {
                kCLAuthorizationStatusNotDetermined -> {
                    println("iOS: Requesting location permission...")
                    // Устанавливаем callback для отслеживания результата
                    locationDelegate.onAuthorizationChange = { newStatus ->
                        handleLocationAuthorizationChange(newStatus)
                    }
                    locationManager.requestWhenInUseAuthorization()
                }
                kCLAuthorizationStatusAuthorizedWhenInUse -> {
                    println("iOS: Requesting background location permission...")
                    // Устанавливаем callback для отслеживания результата
                    locationDelegate.onAuthorizationChange = { newStatus ->
                        handleLocationAuthorizationChange(newStatus)
                    }
                    locationManager.requestAlwaysAuthorization()
                }
                kCLAuthorizationStatusAuthorizedAlways -> {
                    println("iOS: Location permission already granted, requesting notifications...")
                    // Местоположение уже разрешено, запрашиваем уведомления
                    requestNotificationPermissions()
                }
                kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> {
                    println("iOS: Location permission denied or restricted, stopping permission flow")
                    // Местоположение отклонено - останавливаем процесс
                    // Не запрашиваем остальные разрешения
                }
                else -> {
                    println("iOS: Unknown authorization status: $status")
                }
            }
        }
    }

    private fun handleLocationAuthorizationChange(newStatus: Int) {
        dispatch_async(dispatch_get_main_queue()) {
            println("iOS: Handling authorization change: $newStatus")

            when (newStatus) {
                kCLAuthorizationStatusAuthorizedWhenInUse.toInt() -> {
                    println("iOS: Location permission granted, requesting background permission...")
                    // Получили разрешение на местоположение, запрашиваем фоновое
                    locationManager.requestAlwaysAuthorization()
                }
                kCLAuthorizationStatusAuthorizedAlways.toInt() -> {
                    println("iOS: Background location permission granted, requesting notifications...")
                    // Получили фоновое разрешение, запрашиваем уведомления
                    locationDelegate.onAuthorizationChange = null
                    requestNotificationPermissions()
                }
                kCLAuthorizationStatusDenied.toInt(), kCLAuthorizationStatusRestricted.toInt() -> {
                    println("iOS: Location permission denied, stopping permission flow")
                    // Пользователь отказал - останавливаем процесс
                    locationDelegate.onAuthorizationChange = null
                }
                else -> {
                    println("iOS: Unexpected authorization status: $newStatus")
                }
            }
        }
    }

    private fun requestNotificationPermissions() {
        dispatch_async(dispatch_get_main_queue()) {
            println("iOS: Requesting notification permission...")
            val center = UNUserNotificationCenter.currentNotificationCenter()
            center.requestAuthorizationWithOptions(
                options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound,
                completionHandler = { granted, error ->
                    if (granted) {
                        println("iOS: Notification permission granted")
                    } else {
                        println("iOS: Notification permission denied: ${error?.localizedDescription}")
                    }
                },
            )
        }
    }
}
