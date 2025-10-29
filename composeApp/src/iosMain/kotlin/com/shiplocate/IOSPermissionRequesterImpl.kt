package com.shiplocate

import com.shiplocate.data.datasource.PermissionRequester
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreMotion.CMMotionActivityManager
import platform.Foundation.NSDate
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.Foundation.dateWithTimeIntervalSinceNow
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
 * iOS реализация PermissionRequester
 */
class IOSPermissionRequesterImpl : PermissionRequester {
    // Lazy инициализация - создается только при первом обращении
    private val locationManager: CLLocationManager by lazy {
        val manager = CLLocationManager()
        manager.delegate = locationDelegate
        manager
    }
    private val locationDelegate = LocationManagerDelegate()

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

    override suspend fun hasActivityRecognitionPermission(): Boolean {
        return suspendCoroutine { continuation ->
            dispatch_async(dispatch_get_main_queue()) {
                try {
                    val motionManager = CMMotionActivityManager()
                    
                    // Проверяем разрешение через queryActivityStartingFromDate
                    val now = NSDate()
                    val oneDayAgo = NSDate.dateWithTimeIntervalSinceNow(-86400.0)
                    
                    motionManager.queryActivityStartingFromDate(
                        start = oneDayAgo,
                        toDate = now,
                        toQueue = NSOperationQueue.mainQueue
                    ) { activities, error ->
                        val hasPermission = error == null || 
                            !(error.localizedDescription.contains("denied") || error.localizedDescription.contains("permission"))
                        continuation.resumeWith(Result.success(hasPermission))
                    }
                } catch (e: Exception) {
                    // При ошибке считаем, что разрешения нет
                    continuation.resumeWith(Result.success(false))
                }
            }
        }
    }

    override suspend fun isBatteryOptimizationDisabled(): Boolean {
        // iOS не имеет концепции battery optimization как Android
        // Возвращаем true, так как iOS управляет батареей автоматически
        return true
    }

    override suspend fun requestAllPermissions(): Result<Unit> {
        return try {
            requestAllPermissionsSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestNotificationPermission(): Result<Unit> {
        return try {
            requestNotificationPermissionsSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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

    private fun requestAllPermissionsSync() {
        dispatch_async(dispatch_get_main_queue()) {
            val status = locationManager.authorizationStatus
            println("iOS: Current authorization status: $status")

            when (status) {
                kCLAuthorizationStatusNotDetermined -> {
                    println("iOS: Requesting location permission...")
                    locationDelegate.onAuthorizationChange = { newStatus ->
                        handleLocationAuthorizationChange(newStatus)
                    }
                    locationManager.requestWhenInUseAuthorization()
                }
                kCLAuthorizationStatusAuthorizedWhenInUse -> {
                    println("iOS: Requesting background location permission...")
                    locationDelegate.onAuthorizationChange = { newStatus ->
                        handleLocationAuthorizationChange(newStatus)
                    }
                    locationManager.requestAlwaysAuthorization()
                }
                kCLAuthorizationStatusAuthorizedAlways -> {
                    println("iOS: Location permission already granted, requesting notifications...")
                    requestNotificationPermissions()
                }
                kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> {
                    println("iOS: Location permission denied or restricted, stopping permission flow")
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
                    locationManager.requestAlwaysAuthorization()
                }
                kCLAuthorizationStatusAuthorizedAlways.toInt() -> {
                    println("iOS: Background location permission granted, requesting notifications...")
                    locationDelegate.onAuthorizationChange = null
                    requestNotificationPermissions()
                }
                kCLAuthorizationStatusDenied.toInt(), kCLAuthorizationStatusRestricted.toInt() -> {
                    println("iOS: Location permission denied, stopping permission flow")
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

    private suspend fun requestNotificationPermissionsSync() {
        return suspendCancellableCoroutine { continuation ->
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
                        continuation.resumeWith(Result.success(Unit))
                    },
                )
            }
        }
    }
}
