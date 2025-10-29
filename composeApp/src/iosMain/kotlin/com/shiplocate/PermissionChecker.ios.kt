package com.shiplocate

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreMotion.CMMotionActivityManager
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.dispatch.dispatch_after
import platform.dispatch.dispatch_time
import platform.dispatch.DISPATCH_TIME_NOW
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.time.Duration.Companion.seconds

/**
 * iOS реализация проверки разрешений
 */
actual class PermissionChecker(
    private val logger: Logger,
) {
    // Lazy инициализация - создается только при первом обращении
    private val locationManager: CLLocationManager by lazy {
        CLLocationManager()
    }

    actual suspend fun hasLocationPermissions(): Boolean {
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
                val center = UNUserNotificationCenter.currentNotificationCenter()
                center.getNotificationSettingsWithCompletionHandler { settings ->
                    val hasPermission = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
                    continuation.resumeWith(Result.success(hasPermission))
                }
            }
        }
    }

    actual suspend fun hasActivityRecognitionPermission(): Boolean {
        return suspendCoroutine { continuation ->
            dispatch_async(dispatch_get_main_queue()) {
                try {
                    val motionManager = CMMotionActivityManager()
                    
                    // Проверяем разрешение через queryActivityStarting
                    // Если разрешение есть, запрос пройдет успешно
                    val now = platform.Foundation.NSDate()
                    val oneDayAgo = platform.Foundation.NSDate.dateWithTimeIntervalSinceNow(-86400.0)
                    
                    motionManager.queryActivityStarting(
                        fromDate = oneDayAgo,
                        toDate = now,
                        queue = platform.dispatch.get_main_queue()
                    ) { activities, error ->
                        // Если нет ошибки доступа - разрешение есть
                        val hasPermission = error == null || 
                            !(error.localizedDescription?.contains("denied") == true ||
                              error.localizedDescription?.contains("permission") == true)
                        logger.debug(LogCategory.PERMISSIONS, "iOS: Motion permission check: hasPermission=$hasPermission, error=${error?.localizedDescription}")
                        continuation.resumeWith(Result.success(hasPermission))
                    }
                } catch (e: Exception) {
                    // При ошибке считаем, что разрешения нет
                    logger.debug(LogCategory.PERMISSIONS, "iOS: Motion permission check exception: ${e.message}")
                    continuation.resumeWith(Result.success(false))
                }
            }
        }
    }

    actual suspend fun requestActivityRecognitionPermission(): Result<Unit> {
        return suspendCoroutine { continuation ->
            dispatch_async(dispatch_get_main_queue()) {
                try {
                    val motionManager = CMMotionActivityManager()
                    var callbackCalled = false
                    
                    // Используем startActivityUpdates для запроса разрешения
                    // Это гарантированно покажет диалог при первом обращении
                    logger.debug(LogCategory.PERMISSIONS, "iOS: Requesting motion permission via startActivityUpdates")
                    
                    // Таймаут на случай, если callback не сработает (например, если пользователь не ответил на диалог)
                    val timeoutDelay = 5.seconds
                    val timeoutTime = dispatch_time(DISPATCH_TIME_NOW, timeoutDelay.inWholeNanoseconds.toLong())
                    
                    dispatch_after(timeoutTime, platform.dispatch.get_main_queue()) {
                        if (!callbackCalled) {
                            callbackCalled = true
                            logger.debug(LogCategory.PERMISSIONS, "iOS: Motion permission request timeout")
                            motionManager.stopActivityUpdates()
                            continuation.resumeWith(Result.success(Unit))
                        }
                    }
                    
                    // Запускаем обновления - это точно покажет диалог при первом обращении
                    motionManager.startActivityUpdates(
                        queue = platform.dispatch.get_main_queue()
                    ) { activity ->
                        if (!callbackCalled) {
                            callbackCalled = true
                            
                            // Останавливаем обновления сразу после первого callback
                            // (это означает, что разрешение получено или уже было)
                            motionManager.stopActivityUpdates()
                            
                            logger.debug(LogCategory.PERMISSIONS, "iOS: Motion permission granted or already had")
                            continuation.resumeWith(Result.success(Unit))
                        }
                    }
                } catch (e: Exception) {
                    logger.error(LogCategory.PERMISSIONS, "iOS: Error requesting motion permission: ${e.message}", e)
                    continuation.resumeWith(Result.success(Unit))
                }
            }
        }
    }

    actual suspend fun hasAllRequiredPermissions(): Boolean {
        val location = hasLocationPermissions()
        val background = hasBackgroundLocationPermission()
        val notifications = hasNotificationPermission()
        val activityRecognition = hasActivityRecognitionPermission()
        return location && background && notifications && activityRecognition
    }

    actual suspend fun getPermissionStatusMessage(): String {
        val location = hasLocationPermissions()
        val background = hasBackgroundLocationPermission()
        val notifications = hasNotificationPermission()
        val activityRecognition = hasActivityRecognitionPermission()

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
        if (!activityRecognition) {
            missingPermissions.add("Activity Recognition")
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
        // Запрашиваем разрешения на местоположение
        requestLocationPermissions()
        
        // Запрашиваем разрешение на Motion (чтобы диалог показался когда пользователь активен)
        // Используем корутину для асинхронного запроса
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            try {
                requestActivityRecognitionPermission()
            } catch (e: Exception) {
                logger.error(LogCategory.PERMISSIONS, "iOS: Error requesting motion permission: ${e.message}", e)
            }
        }
        
        // Запрашиваем разрешения на уведомления
        requestNotificationPermissions()
    }

    actual fun requestNotificationPermission() {
        // Запрашиваем только разрешения на уведомления
        requestNotificationPermissions()
    }

    private fun requestLocationPermissions() {
        dispatch_async(dispatch_get_main_queue()) {
            when (locationManager.authorizationStatus) {
                kCLAuthorizationStatusNotDetermined -> {
                    // Запрашиваем разрешение на использование местоположения
                    locationManager.requestWhenInUseAuthorization()
                }
                kCLAuthorizationStatusAuthorizedWhenInUse -> {
                    // Запрашиваем разрешение на использование в фоне
                    locationManager.requestAlwaysAuthorization()
                }
                else -> {
                    // Разрешение уже получено или отклонено
                }
            }
        }
    }

    private fun requestNotificationPermissions() {
        dispatch_async(dispatch_get_main_queue()) {
            val center = UNUserNotificationCenter.currentNotificationCenter()
            center.requestAuthorizationWithOptions(
                options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound,
                completionHandler = { granted, error ->
                    if (granted) {
                        logger.debug(LogCategory.PERMISSIONS, "iOS: Notification permission granted")
                    } else {
                        logger.debug(LogCategory.PERMISSIONS, "iOS: Notification permission denied: ${error?.localizedDescription}")
                    }
                },
            )
        }
    }
}
