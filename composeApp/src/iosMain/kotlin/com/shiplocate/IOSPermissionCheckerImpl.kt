package com.shiplocate

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.PermissionChecker
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
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSObject
import platform.darwin.dispatch_after
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.seconds

/**
 * iOS реализация PermissionChecker
 */
class IOSPermissionCheckerImpl(
    private val logger: Logger,
) : PermissionChecker {
    // Lazy инициализация - создается только при первом обращении
    private val locationManager: CLLocationManager by lazy {
        val manager = CLLocationManager()
        manager.delegate = locationDelegate
        manager
    }
    private val locationDelegate = LocationManagerDelegate()

    // Делегат для отслеживания изменений статуса разрешений
    private inner class LocationManagerDelegate : NSObject(), CLLocationManagerDelegateProtocol {
        var onAuthorizationChange: ((Int) -> Unit)? = null

        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            val status = manager.authorizationStatus
            logger.debug(LogCategory.PERMISSIONS, "iOS: Location authorization changed to: $status")
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
                    
                    // Проверяем разрешение через queryActivityStarting
                    val now = NSDate()
                    val oneDayAgo = NSDate.dateWithTimeIntervalSinceNow(-86400.0)
                    
                    motionManager.queryActivityStartingFromDate(
                        start = oneDayAgo,
                        toDate = now,
                        toQueue = NSOperationQueue.mainQueue
                    ) { activities, error ->
                        val hasPermission = error == null || 
                            !(error.localizedDescription?.contains("denied") == true ||
                              error.localizedDescription?.contains("permission") == true)
                        logger.debug(LogCategory.PERMISSIONS, "iOS: Motion permission check: hasPermission=$hasPermission")
                        continuation.resumeWith(Result.success(hasPermission))
                    }
                } catch (e: Exception) {
                    logger.debug(LogCategory.PERMISSIONS, "iOS: Motion permission check exception: ${e.message}")
                    continuation.resumeWith(Result.success(false))
                }
            }
        }
    }

    override suspend fun hasAllRequiredPermissions(): Boolean {
        val location = hasLocationPermissions()
        val background = hasBackgroundLocationPermission()
        val notifications = hasNotificationPermission()
        val activityRecognition = hasActivityRecognitionPermission()
        return location && background && notifications && activityRecognition
    }

    override suspend fun getPermissionStatusMessage(): String {
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
            logger.debug(LogCategory.PERMISSIONS, "iOS: Current authorization status: $status")

            when (status) {
                kCLAuthorizationStatusNotDetermined -> {
                    logger.debug(LogCategory.PERMISSIONS, "iOS: Requesting location permission...")
                    // Устанавливаем callback для отслеживания результата
                    locationDelegate.onAuthorizationChange = { newStatus ->
                        handleLocationAuthorizationChange(newStatus)
                    }
                    locationManager.requestWhenInUseAuthorization()
                }
                kCLAuthorizationStatusAuthorizedWhenInUse -> {
                    logger.debug(LogCategory.PERMISSIONS, "iOS: Requesting background location permission...")
                    // Устанавливаем callback для отслеживания результата
                    locationDelegate.onAuthorizationChange = { newStatus ->
                        handleLocationAuthorizationChange(newStatus)
                    }
                    locationManager.requestAlwaysAuthorization()
                }
                kCLAuthorizationStatusAuthorizedAlways -> {
                    logger.debug(LogCategory.PERMISSIONS, "iOS: Location permission already granted, requesting motion and notifications...")
                    // Местоположение уже разрешено, запрашиваем Motion и уведомления
                    requestMotionPermission()
                    requestNotificationPermissions()
                }
                kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> {
                    logger.debug(LogCategory.PERMISSIONS, "iOS: Location permission denied or restricted, stopping permission flow")
                    // Местоположение отклонено - останавливаем процесс
                    // Не запрашиваем остальные разрешения
                }
                else -> {
                    logger.debug(LogCategory.PERMISSIONS, "iOS: Unknown authorization status: $status")
                }
            }
        }
    }

    private fun handleLocationAuthorizationChange(newStatus: Int) {
        dispatch_async(dispatch_get_main_queue()) {
            logger.debug(LogCategory.PERMISSIONS, "iOS: Handling authorization change: $newStatus")

            when (newStatus) {
                kCLAuthorizationStatusAuthorizedWhenInUse.toInt() -> {
                    logger.debug(LogCategory.PERMISSIONS, "iOS: Location permission granted, requesting background permission...")
                    // Получили разрешение на местоположение, запрашиваем фоновое
                    locationManager.requestAlwaysAuthorization()
                }
                kCLAuthorizationStatusAuthorizedAlways.toInt() -> {
                    logger.debug(LogCategory.PERMISSIONS, "iOS: Background location permission granted, requesting motion and notifications...")
                    // Получили фоновое разрешение, запрашиваем Motion и уведомления
                    locationDelegate.onAuthorizationChange = null
                    requestMotionPermission()
                    requestNotificationPermissions()
                }
                kCLAuthorizationStatusDenied.toInt(), kCLAuthorizationStatusRestricted.toInt() -> {
                    logger.debug(LogCategory.PERMISSIONS, "iOS: Location permission denied, stopping permission flow")
                    // Пользователь отказал - останавливаем процесс
                    locationDelegate.onAuthorizationChange = null
                }
                else -> {
                    logger.debug(LogCategory.PERMISSIONS, "iOS: Unexpected authorization status: $newStatus")
                }
            }
        }
    }

    private fun requestNotificationPermissions() {
        dispatch_async(dispatch_get_main_queue()) {
            logger.debug(LogCategory.PERMISSIONS, "iOS: Requesting notification permission...")
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

    override fun requestNotificationPermission() {
        requestNotificationPermissions()
    }
    
    private fun requestMotionPermission() {
        dispatch_async(dispatch_get_main_queue()) {
            logger.debug(LogCategory.PERMISSIONS, "iOS: Requesting motion permission...")
            try {
                val motionManager = CMMotionActivityManager()
                var callbackCalled = false
                
                // Проверяем, есть ли уже разрешение
                val now = NSDate()
                val oneDayAgo = NSDate.dateWithTimeIntervalSinceNow(-86400.0)
                
                // Сначала проверяем через queryActivityStarting
                motionManager.queryActivityStartingFromDate(
                    start = oneDayAgo,
                    toDate = now,
                    toQueue =  NSOperationQueue.mainQueue
                ) { activities, error ->
                    if (error == null) {
                        // Разрешение уже есть
                        logger.debug(LogCategory.PERMISSIONS, "iOS: Motion permission already granted")
                    } else {
                        // Разрешения нет, используем startActivityUpdates для запроса
                        logger.debug(LogCategory.PERMISSIONS, "iOS: Motion permission not granted, requesting via startActivityUpdates")
                        
                        // Таймаут на случай, если callback не сработает
                        val timeoutDelay = 5.seconds
                        val timeoutTime = dispatch_time(DISPATCH_TIME_NOW, timeoutDelay.inWholeNanoseconds.toLong())
                        
                        dispatch_after(timeoutTime, dispatch_get_main_queue()) {
                            if (!callbackCalled) {
                                callbackCalled = true
                                logger.debug(LogCategory.PERMISSIONS, "iOS: Motion permission request timeout")
                                motionManager.stopActivityUpdates()
                            }
                        }
                        
                        // Запускаем обновления - это точно покажет диалог при первом обращении
                        motionManager.startActivityUpdatesToQueue(
                            queue = NSOperationQueue.mainQueue
                        ) { activity ->
                            if (!callbackCalled) {
                                callbackCalled = true
                                
                                // Останавливаем обновления сразу после первого callback
                                motionManager.stopActivityUpdates()
                                
                                logger.debug(LogCategory.PERMISSIONS, "iOS: Motion permission granted")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error(LogCategory.PERMISSIONS, "iOS: Error requesting motion permission: ${e.message}", e)
            }
        }
    }
}
