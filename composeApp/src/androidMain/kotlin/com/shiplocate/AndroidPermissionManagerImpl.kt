package com.shiplocate

import android.content.pm.PackageManager
import android.os.Build
import com.shiplocate.data.datasource.PermissionManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android реализация PermissionRequester
 */
class AndroidPermissionManagerImpl(private val activityContextProvider: ActivityProvider) : PermissionManager, KoinComponent {

    // Lazy инициализация - создается только при первом обращении
    private val permissionRequester: AndroidPermissionRequester by lazy {
        AndroidPermissionRequester(activityContextProvider.getActivity())
    }

    private val permissionChecker: AndroidPermissionChecker by inject()


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
            println("AndroidPermissionRequester.requestAllPermissions() called")

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

            println("No dialogs blocked, continuing with permission request")
            continuePermissionRequest()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestNotificationPermission(): Result<Unit> {
        return try {
            println("AndroidPermissionRequester.requestNotificationPermission() called")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // API 33+ (Android 13+): запрашиваем разрешение POST_NOTIFICATIONS
                if (!permissionChecker.hasNotificationPermission()) {
                    if (permissionRequester.shouldShowNotificationPermissionRationale()) {
                        println("Showing notification permission rationale")
                        // Показываем объяснение зачем нужны уведомления
                    }

                    println("Requesting notification permission")
                    permissionRequester.requestNotificationPermission()
                } else {
                    println("Notification permission already granted")
                }
            } else {
                // API 24-32 (Android 7.0-12): уведомления работают без разрешения
                println("Notification permission not required for this Android version (API ${Build.VERSION.SDK_INT})")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun continuePermissionRequest() {
        println("AndroidPermissionRequester.continuePermissionRequest() called")

        // Продолжаем с того места, где остановились
        if (!permissionChecker.hasLocationPermissions()) {
            // Если основные разрешения не получены, начинаем сначала
            fun requestLocationPermissions() {
                println("AndroidPermissionRequester.requestLocationPermissions() called")
                if (!permissionChecker.hasLocationPermissions()) {
                    if (permissionRequester.shouldShowLocationPermissionRationale()) {
                        println("Showing location permission rationale")
                        // Показываем объяснение зачем нужны разрешения
                    }

                    println("Requesting location permissions")
                    permissionRequester.requestLocationPermissions()
                } else {
                    println("Location permissions already granted")
                }
            }
        } else if (!permissionChecker.hasBackgroundLocationPermission()) {
            // Если основные разрешения есть, но фоновое нет - запрашиваем фоновое
            println("AndroidPermissionRequester.requestBackgroundLocationPermission() called")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // API 29+ (Android 10+): запрашиваем отдельное разрешение ACCESS_BACKGROUND_LOCATION
                if (!permissionChecker.hasBackgroundLocationPermission()) {
                    println("Requesting background location permission")
                    permissionRequester.requestBackgroundLocationPermission()
                } else {
                    println("Background location permission already granted")
                }
            } else {
                // API 24-28 (Android 7.0-9): фоновое разрешение не требуется
                println("Background location permission not required for this Android version (API ${Build.VERSION.SDK_INT})")
            }
        } else if (!permissionChecker.hasActivityRecognitionPermission()) {
            // Если основные разрешения есть, но Activity Recognition нет - запрашиваем Activity Recognition
            println("AndroidPermissionRequester.requestActivityRecognitionPermission() called")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // API 29+ (Android 10+): запрашиваем разрешение ACTIVITY_RECOGNITION
                if (!permissionChecker.hasActivityRecognitionPermission()) {
                    println("Requesting activity recognition permission")
                    permissionRequester.requestActivityRecognitionPermission()
                } else {
                    println("Activity recognition permission already granted")
                }
            } else {
                // API 24-28 (Android 7.0-9): Activity Recognition доступен через Google Play Services
                // без необходимости в системном разрешении - пропускаем запрос
                println("Activity recognition available without permission for this Android version (API ${Build.VERSION.SDK_INT})")
            }
        } else if (!permissionChecker.hasNotificationPermission()) {
            // Если основные разрешения есть, но уведомления нет - запрашиваем уведомления
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // API 33+ (Android 13+): запрашиваем разрешение POST_NOTIFICATIONS
                if (!permissionChecker.hasNotificationPermission()) {
                    if (permissionRequester.shouldShowNotificationPermissionRationale()) {
                        println("Showing notification permission rationale")
                        // Показываем объяснение зачем нужны уведомления
                    }

                    println("Requesting notification permission")
                    permissionRequester.requestNotificationPermission()
                } else {
                    println("Notification permission already granted")
                }
            } else {
                // API 24-32 (Android 7.0-12): уведомления работают без разрешения
                println("Notification permission not required for this Android version (API ${Build.VERSION.SDK_INT})")
            }
        } else if (!permissionChecker.isBatteryOptimizationDisabled()) {
            // Если все разрешения есть, но оптимизация батареи включена - запрашиваем отключение
            permissionRequester.requestBatteryOptimizationDisable()
        } else {
            // Все разрешения получены
            println("All permissions granted")
        }
    }

    fun handlePermissionResult(
        requestCode: Int,
        grantResults: IntArray,
    ) {
        println("AndroidPermissionRequester.handlePermissionResult() called with requestCode: $requestCode")

        when (requestCode) {
            MainActivity.LOCATION_PERMISSION_REQUEST_CODE -> {
                val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                println("Location permissions result: $allGranted")

                if (allGranted) {
                    // Основные разрешения получены, продолжаем с фоновым разрешением
                    continuePermissionRequest()
                } else {
                    // Пользователь отказал в разрешениях
                    // Проверяем, был ли диалог заблокирован (состояние 4: Don't ask again)
                    val rationale = permissionRequester.shouldShowLocationPermissionRationale()
                    println("Location permissions denied, rationale: $rationale")

                    // rationale = false после отказа означает, что диалог был заблокирован
                    // Нужно открыть настройки приложения
                    println("Location permission dialog blocked, opening settings")
                    permissionRequester.openLocationSettings()
                }
            }

            MainActivity.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> {
                val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                println("Background location permission result: $granted")

                if (granted) {
                    // Фоновое разрешение получено, продолжаем с Activity Recognition
                    continuePermissionRequest()
                } else {
                    // Пользователь отказал в фоновом разрешении
                    // Проверяем, был ли диалог заблокирован (состояние 4: Don't ask again)
                    val rationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        permissionRequester.shouldShowBackgroundLocationPermissionRationale()
                    } else {
                        false
                    }
                    println("Background location permission denied, rationale: $rationale")

                    if (!rationale && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // rationale = false после отказа означает, что диалог был заблокирован
                        // Нужно открыть настройки приложения
                        println("Background location permission dialog blocked, opening settings")
                        permissionRequester.openLocationSettings()
                    } else {
                        // Пользователь просто отказал, но можно попробовать снова
                        println("Background location permission denied by user")
                    }
                }
            }

            MainActivity.ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE -> {
                val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                println("Activity recognition permission result: $granted")

                if (granted) {
                    // Activity Recognition разрешение получено, продолжаем с уведомлениями
                    continuePermissionRequest()
                } else {
                    // Пользователь отказал в Activity Recognition разрешении
                    // Проверяем, был ли диалог заблокирован (состояние 4: Don't ask again)
                    val rationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        permissionRequester.shouldShowActivityRecognitionPermissionRationale()
                    } else {
                        false
                    }
                    println("Activity recognition permission denied, rationale: $rationale")

                    if (!rationale && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // rationale = false после отказа означает, что диалог был заблокирован
                        // Нужно открыть настройки приложения
                        println("Activity recognition permission dialog blocked, opening settings")
                        permissionRequester.openLocationSettings() // Activity Recognition обычно находится в настройках разрешений вместе с геолокацией
                    } else {
                        // Пользователь просто отказал, но можно попробовать снова
                        println("Activity recognition permission denied by user")
                    }
                }
            }

            MainActivity.REQUEST_NOTIFICATIONS_PERMISSION -> {
                // Обрабатываем результат запроса только уведомлений
                val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                println("Notification permission result: $granted")

                if (granted) {
                    // Уведомления получены - завершаем процесс (не продолжаем с другими разрешениями)
                    println("Notification permission granted, permission request process completed")
                } else {
                    // Пользователь отказал в уведомлениях
                    // Проверяем, был ли диалог заблокирован (состояние 4: Don't ask again)
                    val rationale = permissionRequester.shouldShowNotificationPermissionRationale()
                    println("Notification permission denied, rationale: $rationale")

                    if (!rationale && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // rationale = false после отказа означает, что диалог был заблокирован
                        // Нужно открыть настройки уведомлений
                        println("Notification permission dialog blocked, opening settings")
                        permissionRequester.openNotificationSettings()
                    } else {
                        // Пользователь просто отказал, но можно попробовать снова
                        println("Notification permission denied by user")
                    }
                }
            }
        }
    }
}
