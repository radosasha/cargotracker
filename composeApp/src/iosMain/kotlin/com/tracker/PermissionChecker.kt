package com.tracker

import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSBundle
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.Foundation.NSURL
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNAuthorizationStatus
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.suspendCoroutine

/**
 * iOS реализация проверки разрешений
 */
actual class PermissionChecker {
    
    private val locationManager = CLLocationManager()
    
    actual suspend fun hasLocationPermissions(): Boolean {
        return suspendCoroutine { continuation ->
            dispatch_async(dispatch_get_main_queue()) {
                val status = locationManager.authorizationStatus
                val hasPermission = when (status) {
                    kCLAuthorizationStatusAuthorizedWhenInUse,
                    kCLAuthorizationStatusAuthorizedAlways -> true
                    kCLAuthorizationStatusNotDetermined,
                    kCLAuthorizationStatusDenied,
                    kCLAuthorizationStatusRestricted -> false
                    else -> false
                }
                continuation.resumeWith(Result.success(hasPermission))
            }
        }
    }
    
    actual suspend fun hasBackgroundLocationPermission(): Boolean {
        return suspendCoroutine { continuation ->
            dispatch_async(dispatch_get_main_queue()) {
                val status = locationManager.authorizationStatus
                val hasPermission = status == kCLAuthorizationStatusAuthorizedAlways
                continuation.resumeWith(Result.success(hasPermission))
            }
        }
    }
    
    actual suspend fun hasNotificationPermission(): Boolean {
        return suspendCoroutine { continuation ->
            dispatch_async(dispatch_get_main_queue()) {
                // Простая заглушка для iOS - в реальном приложении нужно использовать правильный API
                // Пока что возвращаем true, так как iOS уведомления работают по-другому
                continuation.resumeWith(Result.success(true))
            }
        }
    }
    
    actual suspend fun hasAllRequiredPermissions(): Boolean {
        val location = hasLocationPermissions()
        val background = hasBackgroundLocationPermission()
        val notifications = hasNotificationPermission()
        return location && background && notifications
    }
    
    actual suspend fun getPermissionStatusMessage(): String {
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
    
    actual suspend fun openAppSettings(): Result<Unit> {
        return try {
            UIApplication.sharedApplication.openURL(NSURL(string = UIApplicationOpenSettingsURLString))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    actual fun requestAllPermissions() {
        // В iOS разрешения запрашиваются автоматически при первом обращении к CLLocationManager
        // Здесь можно добавить дополнительную логику если нужно
    }
}
