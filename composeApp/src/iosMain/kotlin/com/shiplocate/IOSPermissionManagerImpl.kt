package com.shiplocate

import com.shiplocate.data.datasource.PermissionManager
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreMotion.CMAuthorizationStatusAuthorized
import platform.CoreMotion.CMAuthorizationStatusDenied
import platform.CoreMotion.CMAuthorizationStatusNotDetermined
import platform.CoreMotion.CMAuthorizationStatusRestricted
import platform.CoreMotion.CMMotionActivityManager
import platform.Foundation.NSDate
import platform.Foundation.NSOperationQueue
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS реализация PermissionRequester
 */
class IOSPermissionManagerImpl : PermissionManager {
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
                    // Используем authorizationStatus для проверки статуса без запроса разрешения
                    val status = CMMotionActivityManager.authorizationStatus()
                    val hasPermission = when (status) {
                        CMAuthorizationStatusAuthorized -> true
                        CMAuthorizationStatusNotDetermined,
                        CMAuthorizationStatusRestricted,
                        CMAuthorizationStatusDenied,
                            -> false

                        else -> false
                    }
                    println("iOS: Motion permission check - status: $status, hasPermission: $hasPermission")
                    continuation.resumeWith(Result.success(hasPermission))
                } catch (e: Exception) {
                    println("iOS: Motion permission check exception: ${e.message}")
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
        if (!hasLocationPermissions()) {
            val result = requestLocationPermissionInternal()
            if (result.isFailure) {
                return result
            }
        }
        if (!hasBackgroundLocationPermission()) {
            val result = requestBackgroundLocationPermissionInternal()
            if (result.isFailure) {
                return result
            }
        }
        if (!hasNotificationPermission()) {
            val result = requestNotificationPermissions()
            if (result.isFailure) {
                return result
            }
        }
        if (!hasActivityRecognitionPermission()) {
            val result = requestMotionPermission()
            if (result.isFailure) {
                return result
            }
        }
        return Result.success(Unit)
    }

    override suspend fun requestLocationPermission(): Result<Unit> {
        return if (!hasLocationPermissions()) {
            requestLocationPermissionInternal()
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun requestBackgroundLocationPermission(): Result<Unit> {
        return if (!hasBackgroundLocationPermission()) {
            requestBackgroundLocationPermissionInternal()
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun requestBatteryOptimizationDisable(): Result<Unit> {
        // iOS не требует отдельного разрешения для батареи
        return Result.success(Unit)
    }

    override suspend fun requestEnableHighAccuracy(): Result<Unit> {
        // на ios не требует отдельного пермишена
        return Result.success(Unit)
    }

    override suspend fun openAirplaneModeSettings(): Result<Unit> {
        // нет публичного API для открытия этого экрана
        return Result.success(Unit)
    }

    private suspend fun requestLocationPermissionInternal(): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            dispatch_async(dispatch_get_main_queue()) {
                locationDelegate.onAuthorizationChange = { newStatus ->
                    when (newStatus) {
                        kCLAuthorizationStatusAuthorizedWhenInUse,
                        kCLAuthorizationStatusAuthorizedAlways,
                            -> {
                            locationDelegate.onAuthorizationChange = null
                            continuation.resume(Result.success(Unit))
                        }

                        kCLAuthorizationStatusDenied.toInt(), kCLAuthorizationStatusRestricted.toInt() -> {
                            locationDelegate.onAuthorizationChange = null
                            continuation.resume(Result.failure(Exception("Location permission denied")))
                        }

                        else -> {
                            continuation.resume(Result.failure(Exception("Location permission status: ${newStatus}")))
                        }
                    }
                }
            }
            locationManager.requestWhenInUseAuthorization()
        }
    }

    private suspend fun requestMotionPermission(): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            dispatch_async(dispatch_get_main_queue()) {
                // Проверяем статус Motion разрешения
                val motionStatus = CMMotionActivityManager.authorizationStatus()
                println("iOS: Motion permission status: $motionStatus")

                when (motionStatus) {
                    CMAuthorizationStatusAuthorized -> {
                        // Разрешение уже есть, сразу переходим к уведомлениям
                        println("iOS: Motion permission already granted, requesting notifications...")
                        continuation.resume(Result.success(Unit))
                    }

                    CMAuthorizationStatusNotDetermined -> {
                        // Разрешение не запрашивалось, запрашиваем
                        println("iOS: Requesting motion permission...")
                        val motionManager = CMMotionActivityManager()

                        println("iOS: Requesting motion permission via queryActivityStartingFromDate...")

                        // Используем queryActivityStartingFromDate для запроса разрешения
                        // Это покажет диалог при первом обращении
                        val now = NSDate()
                        val oneDayAgo = NSDate.dateWithTimeIntervalSinceNow(-600.0) // 24 часа назад

                        motionManager.queryActivityStartingFromDate(
                            start = oneDayAgo,
                            toDate = now,
                            toQueue = NSOperationQueue.mainQueue
                        ) { activities, error ->
                            // Проверяем статус разрешения через authorizationStatus
                            val status = CMMotionActivityManager.authorizationStatus()
                            val hasPermission = status == CMAuthorizationStatusAuthorized
                            continuation.resume(
                                if (hasPermission) Result.success(Unit)
                                else Result.failure(Exception("Motion permission denied"))
                            )
                        }
                    }

                    CMAuthorizationStatusDenied, CMAuthorizationStatusRestricted -> {
                        // Разрешение отклонено или ограничено
                        println("iOS: Motion permission denied or restricted")
                        continuation.resume(Result.failure(Exception("Motion permission denied")))
                    }

                    else -> {
                        println("iOS: Unknown motion authorization status: $motionStatus")
                        continuation.resume(Result.failure(Exception("Unknown motion authorization status")))
                    }
                }
            }
        }
    }

    private suspend fun requestNotificationPermissions(): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            dispatch_async(dispatch_get_main_queue()) {
                println("iOS: Requesting notification permission...")
                val center = UNUserNotificationCenter.currentNotificationCenter()
                center.requestAuthorizationWithOptions(
                    options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound,
                    completionHandler = { granted, error ->
                        if (granted) {
                            println("iOS: All permissions granted")
                            continuation.resume(Result.success(Unit))
                        } else {
                            println("iOS: Notification permission denied: ${error?.localizedDescription}")
                            continuation.resume(Result.failure(Exception("Notification permission denied: ${error?.localizedDescription}")))
                        }
                    },
                )
            }
        }
    }

    private suspend fun requestBackgroundLocationPermissionInternal(): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            dispatch_async(dispatch_get_main_queue()) {
                locationDelegate.onAuthorizationChange = { newStatus ->
                    when (newStatus) {
                        kCLAuthorizationStatusAuthorizedAlways.toInt() -> {
                            locationDelegate.onAuthorizationChange = null
                            continuation.resume(Result.success(Unit))
                        }

                        kCLAuthorizationStatusDenied.toInt(), kCLAuthorizationStatusRestricted.toInt() -> {
                            locationDelegate.onAuthorizationChange = null
                            continuation.resume(Result.failure(Exception("Background location permission denied")))
                        }

                        else -> {
                            continuation.resume(Result.failure(Exception("Background location permission denied, status: $newStatus")))
                        }
                    }
                }
            }
            locationManager.requestAlwaysAuthorization()
        }
    }

    override suspend fun requestNotificationPermission(): Result<Unit> {
        return requestNotificationPermissions()
    }
}
