package com.shiplocate

import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.PermissionManager
import com.shiplocate.data.datasource.impl.AndroidGpsManager.Companion.INTERVAL_MS
import com.shiplocate.data.datasource.impl.AndroidGpsManager.Companion.MIN_DISTANCE_M
import com.shiplocate.data.datasource.impl.AndroidGpsManager.Companion.MIN_UPDATE_MS
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.component.KoinComponent
import kotlin.coroutines.resume

/**
 * Android реализация PermissionRequester
 */
class AndroidPermissionManagerImpl(
    private val activityContextProvider: ActivityProvider,
    private val permissionChecker: AndroidPermissionChecker,
    private val logger: Logger,
) : PermissionManager, KoinComponent {

    private val permissionRequester: AndroidPermissionRequester by lazy {
        AndroidPermissionRequester(activityContextProvider, logger)
    }

    private fun logDebug(message: String) = logger.debug(LogCategory.PERMISSIONS, message)
    private fun logInfo(message: String) = logger.info(LogCategory.PERMISSIONS, message)
    private fun logWarn(message: String) = logger.warn(LogCategory.PERMISSIONS, message)
    private fun logError(message: String, error: Throwable? = null) =
        logger.error(LogCategory.PERMISSIONS, message, error)

    override suspend fun hasLocationPermissions(): Boolean {
        return permissionChecker.hasLocationPermissions()
    }

    override suspend fun hasBackgroundLocationPermission(): Boolean {
        return permissionChecker.hasBackgroundLocationPermission()
    }

    override suspend fun hasNotificationPermission(): Boolean {
        return permissionChecker.hasNotificationPermission()
    }

    override suspend fun hasActivityRecognitionPermission(): Boolean {
        return permissionChecker.hasActivityRecognitionPermission()
    }

    override suspend fun isBatteryOptimizationDisabled(): Boolean {
        return permissionChecker.isBatteryOptimizationDisabled()
    }

    override suspend fun requestAllPermissions(): Result<Unit> {
        return try {
            logInfo("Requesting all permissions")

            // Логика определения состояния разрешений (по таблице):
            // 1. GRANTED -> разрешение уже дано
            // 2. DENIED + rationale = false -> может быть первое обращение (состояние 2) или "Don't ask again" (состояние 4)
            //    Различие проявится при вызове requestPermission():
            //    - Состояние 2: диалог появится
            //    - Состояние 4: диалог НЕ появится, получим DENIED в handlePermissionResult()
            // 3. DENIED + rationale = true -> пользователь отказал, но можно показать диалог снова (состояние 3)
            //
            // Мы не можем определить состояние 4 до вызова requestPermission(),
            // поэтому всегда пытаемся вызвать диалог.
            // Если диалог был заблокирован, это будет обработано в handlePermissionResult()

            logDebug("No dialogs blocked, continuing with permission request")
//            continuePermissionRequest()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestNotificationPermission(): Result<Unit> {
        return try {
            logInfo("Requesting notification permission")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!permissionChecker.hasNotificationPermission()) {
                    if (permissionRequester.shouldShowNotificationPermissionRationale()) {
                        logDebug("Showing notification permission rationale")
                    }
                    logInfo("Triggering notification permission dialog")
                    permissionRequester.requestNotificationPermission()
                } else {
                    logDebug("Notification permission already granted")
                }
            } else {
                logDebug("Notification permission not required for API ${Build.VERSION.SDK_INT}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logError("Failed to request notification permission", e)
            Result.failure(e)
        }
    }

    override suspend fun requestLocationPermission(): Result<Unit> {
        return try {
            logInfo("Requesting location permission")
            if (!permissionChecker.hasLocationPermissions()) {
                if (permissionRequester.shouldShowLocationPermissionRationale()) {
                    logDebug("Showing location permission rationale")
                }
                logInfo("Triggering location permission dialog")
                permissionRequester.requestLocationPermissions()
            } else {
                logDebug("Location permission already granted")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logError("Failed to request location permission", e)
            Result.failure(e)
        }
    }

    override suspend fun requestBackgroundLocationPermission(): Result<Unit> {
        return try {
            logInfo("Requesting background location permission")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (!permissionChecker.hasBackgroundLocationPermission()) {
                    logInfo("Triggering background location permission dialog")
                    permissionRequester.requestBackgroundLocationPermission()
                } else {
                    logDebug("Background location permission already granted")
                }
            } else {
                logDebug("Background location permission not required for API ${Build.VERSION.SDK_INT}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logError("Failed to request background location permission", e)
            Result.failure(e)
        }
    }

    override suspend fun requestBatteryOptimizationDisable(): Result<Unit> {
        return try {
            logInfo("Requesting battery optimization disable")
            if (!permissionChecker.isBatteryOptimizationDisabled()) {
                logInfo("Triggering battery optimization settings screen")
                permissionRequester.requestBatteryOptimizationDisable()
            } else {
                logDebug("Battery optimization already disabled")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logError("Failed to request battery optimization disable", e)
            Result.failure(e)
        }
    }

    override suspend fun requestEnableHighAccuracy(): Result<Unit> {
        return try {
            suspendCancellableCoroutine { cont ->
                val activity = activityContextProvider.getActivity()
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    1000L,
                ).setMinUpdateDistanceMeters(MIN_DISTANCE_M)
                    .setMaxUpdateDelayMillis(INTERVAL_MS)
                    .setMinUpdateIntervalMillis(MIN_UPDATE_MS).build()

                val settingsRequest = LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)
                    .setAlwaysShow(true)
                    .build()
                val client = LocationServices.getSettingsClient(activity)
                client.checkLocationSettings(settingsRequest)
                    .addOnSuccessListener {
                        if (cont.isActive) cont.resume(Result.success(Unit))
                    }
                    .addOnFailureListener { exception ->
                        if (exception is ResolvableApiException) {
                            try {
                                val manufacturer = Build.MANUFACTURER
                                if (manufacturer.lowercase().startsWith("samsung") && isHighAccuracyEnabled(activity)) {
                                    if (cont.isActive) cont.resume(Result.success(Unit))
                                } else {
                                    exception.startResolutionForResult(
                                        activity,
                                        MainActivity.REQUEST_ENABLE_GPS,
                                    )
                                }
                                // TODO
                                if (cont.isActive) cont.resume(Result.success(Unit))
                            } catch (sendEx: IntentSender.SendIntentException) {
                                if (cont.isActive) cont.resume(Result.failure(sendEx))
                            }
                        } else {
                            if (cont.isActive) cont.resume(Result.failure(exception))
                        }
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun openAirplaneModeSettings(): Result<Unit> {
        return try {
            permissionRequester.openAirplaneModeSettings()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun handlePermissionResult(
        requestCode: Int,
        grantResults: IntArray,
    ) {
        logDebug("handlePermissionResult called with requestCode=$requestCode")

        when (requestCode) {
            MainActivity.LOCATION_PERMISSION_REQUEST_CODE -> {
                val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                logInfo("Location permissions result: $allGranted")

                if (allGranted) {
                    // Основные разрешения получены, продолжаем с фоновым разрешением
//                    continuePermissionRequest()
                } else {
                    // Пользователь отказал в разрешениях
                    // Проверяем, был ли диалог заблокирован (состояние 4: Don't ask again)
                    val rationale = permissionRequester.shouldShowLocationPermissionRationale()
                    logWarn("Location permissions denied, rationale=$rationale")

                    // rationale = false после отказа означает, что диалог был заблокирован
                    // Нужно открыть настройки приложения
                    logWarn("Location permission dialog blocked, opening settings")
                    permissionRequester.openLocationSettings()
                }
            }

            MainActivity.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> {
                val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                logInfo("Background location permission result: $granted")

                if (granted) {
                    // Фоновое разрешение получено, продолжаем с Activity Recognition
//                    continuePermissionRequest()
                } else {
                    val rationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        permissionRequester.shouldShowBackgroundLocationPermissionRationale()
                    } else {
                        false
                    }
                    logWarn("Background location permission denied, rationale=$rationale")

                    if (!rationale && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        logWarn("Background location permission dialog blocked, opening settings")
                        permissionRequester.openLocationSettings()
                    } else {
                        logWarn("Background location permission denied by user")
                    }
                }
            }

            MainActivity.ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE -> {
                val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                logInfo("Activity recognition permission result: $granted")

                if (granted) {
                    // Activity Recognition разрешение получено, продолжаем с уведомлениями
//                    continuePermissionRequest()
                } else {
                    // Пользователь отказал в Activity Recognition разрешении
                    // Проверяем, был ли диалог заблокирован (состояние 4: Don't ask again)
                    val rationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        permissionRequester.shouldShowActivityRecognitionPermissionRationale()
                    } else {
                        false
                    }
                    logWarn("Activity recognition permission denied, rationale=$rationale")

                    if (!rationale && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        logWarn("Activity recognition permission dialog blocked, opening settings")
                        permissionRequester.openLocationSettings()
                    } else {
                        logWarn("Activity recognition permission denied by user")
                    }
                }
            }

            MainActivity.REQUEST_NOTIFICATIONS_PERMISSION -> {
                // Обрабатываем результат запроса только уведомлений
                val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                logInfo("Notification permission result: $granted")

                if (granted) {
                    logInfo("Notification permission granted, process completed")
                } else {
                    val rationale = permissionRequester.shouldShowNotificationPermissionRationale()
                    logWarn("Notification permission denied, rationale=$rationale")

                    if (!rationale && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        logWarn("Notification permission dialog blocked, opening settings")
                        permissionRequester.openNotificationSettings()
                    } else {
                        logWarn("Notification permission denied by user")
                    }
                }
            }
        }
    }

    fun isHighAccuracyEnabled(context: Context): Boolean {
        return try {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.LOCATION_MODE
            ) == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY
        } catch (e: Exception) {
            false
        }
    }
}
