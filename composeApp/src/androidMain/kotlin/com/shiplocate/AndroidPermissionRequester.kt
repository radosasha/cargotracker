package com.shiplocate

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Android класс для запроса разрешений
 * Использует только Android API для определения состояния разрешений,
 * без использования SharedPreferences
 */
class AndroidPermissionRequester(private val context: Activity) {

    fun hasLocationPermissions(): Boolean {
        val fineLocation =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    fun hasBackgroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29+ (Android 10+): требуется отдельное разрешение ACCESS_BACKGROUND_LOCATION
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // API 24-28 (Android 7.0-9): фоновое разрешение не требуется,
            // достаточно основных разрешений на геолокацию
            hasLocationPermissions()
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33+ (Android 13+): требуется разрешение POST_NOTIFICATIONS
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // API 24-32 (Android 7.0-12): уведомления работают без разрешения
            true
        }
    }

    fun hasActivityRecognitionPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29+ (Android 10+): требуется разрешение ACTIVITY_RECOGNITION
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // API 24-28 (Android 7.0-9): Activity Recognition доступен через Google Play Services
            // без необходимости в системном разрешении - доступ предоставляется автоматически
            true
        }
    }

    fun isBatteryOptimizationDisabled(): Boolean {
        // API 23+ (Android 6.0+): оптимизация батареи доступна
        // Наш minSdk = 24, поэтому всегда проверяем
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val result = powerManager.isIgnoringBatteryOptimizations(context.packageName)
        println("isBatteryOptimizationDisabled(): $result")
        return result
    }

    fun shouldShowLocationPermissionRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }

    fun shouldShowNotificationPermissionRationale(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        } else {
            false
        }
    }

    fun requestLocationPermissions() {
        println("AndroidPermissionRequester.requestLocationPermissions() called")
        if (!hasLocationPermissions()) {
            if (shouldShowLocationPermissionRationale()) {
                println("Showing location permission rationale")
                // Показываем объяснение зачем нужны разрешения
            }

            println("Requesting location permissions")
            ActivityCompat.requestPermissions(
                context,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
                MainActivity.LOCATION_PERMISSION_REQUEST_CODE,
            )
        } else {
            println("Location permissions already granted")
        }
    }

    fun requestBackgroundLocationPermission() {
        println("AndroidPermissionRequester.requestBackgroundLocationPermission() called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29+ (Android 10+): запрашиваем отдельное разрешение ACCESS_BACKGROUND_LOCATION
            if (!hasBackgroundLocationPermission()) {
                println("Requesting background location permission")
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    MainActivity.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE,
                )
            } else {
                println("Background location permission already granted")
            }
        } else {
            // API 24-28 (Android 7.0-9): фоновое разрешение не требуется
            println("Background location permission not required for this Android version (API ${Build.VERSION.SDK_INT})")
        }
    }

    fun requestNotificationPermission() {
        println("AndroidPermissionRequester.requestNotificationPermission() called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33+ (Android 13+): запрашиваем разрешение POST_NOTIFICATIONS
            if (!hasNotificationPermission()) {
                if (shouldShowNotificationPermissionRationale()) {
                    println("Showing notification permission rationale")
                    // Показываем объяснение зачем нужны уведомления
                }

                println("Requesting notification permission")
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    MainActivity.REQUEST_NOTIFICATIONS_PERMISSION,
                )
            } else {
                println("Notification permission already granted")
            }
        } else {
            // API 24-32 (Android 7.0-12): уведомления работают без разрешения
            println("Notification permission not required for this Android version (API ${Build.VERSION.SDK_INT})")
        }
    }

    fun requestActivityRecognitionPermission() {
        println("AndroidPermissionRequester.requestActivityRecognitionPermission() called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29+ (Android 10+): запрашиваем разрешение ACTIVITY_RECOGNITION
            if (!hasActivityRecognitionPermission()) {
                println("Requesting activity recognition permission")
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    MainActivity.ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE,
                )
            } else {
                println("Activity recognition permission already granted")
            }
        } else {
            // API 24-28 (Android 7.0-9): Activity Recognition доступен через Google Play Services
            // без необходимости в системном разрешении - пропускаем запрос
            println("Activity recognition available without permission for this Android version (API ${Build.VERSION.SDK_INT})")
        }
    }

    fun requestAllPermissions() {
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
    }

    fun continuePermissionRequest() {
        println("AndroidPermissionRequester.continuePermissionRequest() called")

        // Продолжаем с того места, где остановились
        if (!hasLocationPermissions()) {
            // Если основные разрешения не получены, начинаем сначала
            requestLocationPermissions()
        } else if (!hasBackgroundLocationPermission()) {
            // Если основные разрешения есть, но фоновое нет - запрашиваем фоновое
            requestBackgroundLocationPermission()
        } else if (!hasActivityRecognitionPermission()) {
            // Если основные разрешения есть, но Activity Recognition нет - запрашиваем Activity Recognition
            requestActivityRecognitionPermission()
        } else if (!hasNotificationPermission()) {
            // Если основные разрешения есть, но уведомления нет - запрашиваем уведомления
            requestNotificationPermission()
        } else if (!isBatteryOptimizationDisabled()) {
            // Если все разрешения есть, но оптимизация батареи включена - запрашиваем отключение
            requestBatteryOptimizationDisable()
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
                    val rationale = shouldShowLocationPermissionRationale()
                    println("Location permissions denied, rationale: $rationale")

                    // rationale = false после отказа означает, что диалог был заблокирован
                    // Нужно открыть настройки приложения
                    println("Location permission dialog blocked, opening settings")
                    openLocationSettings()
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
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            context,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        )
                    } else {
                        false
                    }
                    println("Background location permission denied, rationale: $rationale")

                    if (!rationale && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // rationale = false после отказа означает, что диалог был заблокирован
                        // Нужно открыть настройки приложения
                        println("Background location permission dialog blocked, opening settings")
                        openLocationSettings()
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
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            context,
                            Manifest.permission.ACTIVITY_RECOGNITION,
                        )
                    } else {
                        false
                    }
                    println("Activity recognition permission denied, rationale: $rationale")

                    if (!rationale && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // rationale = false после отказа означает, что диалог был заблокирован
                        // Нужно открыть настройки приложения
                        println("Activity recognition permission dialog blocked, opening settings")
                        openLocationSettings() // Activity Recognition обычно находится в настройках разрешений вместе с геолокацией
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
                    val rationale = shouldShowNotificationPermissionRationale()
                    println("Notification permission denied, rationale: $rationale")

                    if (!rationale && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // rationale = false после отказа означает, что диалог был заблокирован
                        // Нужно открыть настройки уведомлений
                        println("Notification permission dialog blocked, opening settings")
                        openNotificationSettings()
                    } else {
                        // Пользователь просто отказал, но можно попробовать снова
                        println("Notification permission denied by user")
                    }
                }
            }
        }
    }

    fun requestBatteryOptimizationDisable() {
        println("requestBatteryOptimizationDisable() called")
        try {
            val intent =
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            context.startActivity(intent)
            println("Battery optimization request sent")
        } catch (e: Exception) {
            println("Error requesting battery optimization disable: ${e.message}")
            // Fallback to battery optimization settings page
            openBatteryOptimizationSettings()
        }
    }

    fun openLocationSettings() {
        println("openLocationSettings() called")
        try {
            // Открываем конкретную страницу разрешений на местоположение
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                // Добавляем extra для перехода к разрешениям
                putExtra("extra_show_fragment", "com.android.settings.applications.InstalledAppDetails")
                putExtra("extra_show_fragment_args", "permissions")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            println("Location settings opened successfully")
        } catch (e: Exception) {
            println("Error opening location settings: ${e.message}")
            // Fallback: попробуем открыть настройки разрешений напрямую
            try {
                val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(fallbackIntent)
                println("Fallback location settings opened successfully")
            } catch (e2: Exception) {
                println("Error opening fallback location settings: ${e2.message}")
                // Последний fallback - общие настройки приложения
                openAppSettings()
            }
        }
    }

    fun openNotificationSettings() {
        println("openNotificationSettings() called")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent =
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                println("Notification settings opened successfully")
            } else {
                // For older Android versions, open app settings
                openAppSettings()
            }
        } catch (e: Exception) {
            println("Error opening notification settings: ${e.message}")
            // Fallback to general app settings
            openAppSettings()
        }
    }

    fun openBatteryOptimizationSettings() {
        println("openBatteryOptimizationSettings() called")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                println("Battery optimization settings opened successfully")
            } else {
                // For older Android versions, open app settings
                openAppSettings()
            }
        } catch (e: Exception) {
            println("Error opening battery optimization settings: ${e.message}")
            // Fallback to general app settings
            openAppSettings()
        }
    }

    fun openAppSettings() {
        println("openAppSettings() called")
        try {
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            println("App settings opened successfully")
        } catch (e: Exception) {
            println("Error opening app settings: ${e.message}")
        }
    }
}
