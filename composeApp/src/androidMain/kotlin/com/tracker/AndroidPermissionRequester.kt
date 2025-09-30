package com.tracker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Android класс для запроса разрешений
 */
class AndroidPermissionRequester(private val context: Context) {

    // Callback для уведомления о результатах разрешений
    var onPermissionResult: ((Boolean) -> Unit)? = null
    
    // SharedPreferences для отслеживания запросов разрешений
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("permission_requests", Context.MODE_PRIVATE)
    }

    fun hasLocationPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    fun hasBackgroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29+ (Android 10+): требуется отдельное разрешение ACCESS_BACKGROUND_LOCATION
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
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
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // API 24-32 (Android 7.0-12): уведомления работают без разрешения
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

    fun hasAllRequiredPermissions(): Boolean {
        return hasLocationPermissions() &&
            hasBackgroundLocationPermission() &&
            hasNotificationPermission() &&
            isBatteryOptimizationDisabled()
    }

    fun getPermissionStatusMessage(): String {
        val missingPermissions = mutableListOf<String>()

        if (!hasLocationPermissions()) {
            missingPermissions.add("Location access")
        }
        if (!hasBackgroundLocationPermission()) {
            missingPermissions.add("Background location")
        }
        if (!hasNotificationPermission()) {
            missingPermissions.add("Notifications")
        }
        if (!isBatteryOptimizationDisabled()) {
            missingPermissions.add("Battery optimization")
        }

        return if (missingPermissions.isEmpty()) {
            "All permissions granted"
        } else {
            "Missing: ${missingPermissions.joinToString(", ")}"
        }
    }

    fun shouldShowLocationPermissionRationale(): Boolean {
        return if (context is androidx.activity.ComponentActivity) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            false
        }
    }

    fun shouldShowNotificationPermissionRationale(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && context is androidx.activity.ComponentActivity) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            false
        }
    }
    
    private fun hasLocationBeenRequestedBefore(): Boolean {
        return prefs.getBoolean("location_requested", false)
    }
    
    private fun hasNotificationBeenRequestedBefore(): Boolean {
        return prefs.getBoolean("notification_requested", false)
    }
    
    private fun markLocationRequested() {
        prefs.edit().putBoolean("location_requested", true).apply()
    }
    
    private fun markNotificationRequested() {
        prefs.edit().putBoolean("notification_requested", true).apply()
    }

    fun requestLocationPermissions() {
        println("AndroidPermissionRequester.requestLocationPermissions() called")
        if (context is androidx.activity.ComponentActivity) {
            if (!hasLocationPermissions()) {
                if (shouldShowLocationPermissionRationale()) {
                    println("Showing location permission rationale")
                    // Показываем объяснение зачем нужны разрешения
                }

                println("Requesting location permissions")
                markLocationRequested()
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    MainActivity.LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                println("Location permissions already granted")
            }
        } else {
            println("Context is not ComponentActivity: ${context.javaClass}")
        }
    }
    
    fun requestBackgroundLocationPermission() {
        println("AndroidPermissionRequester.requestBackgroundLocationPermission() called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && context is androidx.activity.ComponentActivity) {
            // API 29+ (Android 10+): запрашиваем отдельное разрешение ACCESS_BACKGROUND_LOCATION
            if (!hasBackgroundLocationPermission()) {
                println("Requesting background location permission")
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    MainActivity.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && context is androidx.activity.ComponentActivity) {
            // API 33+ (Android 13+): запрашиваем разрешение POST_NOTIFICATIONS
            if (!hasNotificationPermission()) {
                if (shouldShowNotificationPermissionRationale()) {
                    println("Showing notification permission rationale")
                    // Показываем объяснение зачем нужны уведомления
                }

                println("Requesting notification permission")
                markNotificationRequested()
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    MainActivity.NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            } else {
                println("Notification permission already granted")
            }
        } else {
            // API 24-32 (Android 7.0-12): уведомления работают без разрешения
            println("Notification permission not required for this Android version (API ${Build.VERSION.SDK_INT})")
        }
    }

    fun requestAllPermissions() {
        println("AndroidPermissionRequester.requestAllPermissions() called")

        // Проверяем, можно ли показать диалоги разрешений
        // shouldShowRequestPermissionRationale() возвращает:
        // - false при первом запросе (можно показать диалог)
        // - true если пользователь отказал, но можно показать диалог снова
        // - false если пользователь отказал и выбрал "Don't ask again" (диалог заблокирован)
        
        // Для геолокации: если нет разрешения И диалог заблокирован (rationale = false после отказа)
        val locationDialogBlocked = !hasLocationPermissions() && 
            context is androidx.activity.ComponentActivity && 
            !shouldShowLocationPermissionRationale() &&
            hasLocationBeenRequestedBefore()
        
        // Для уведомлений: если нет разрешения И диалог заблокирован
        val notificationDialogBlocked = !hasNotificationPermission() && 
            context is androidx.activity.ComponentActivity && 
            !shouldShowNotificationPermissionRationale() &&
            hasNotificationBeenRequestedBefore()

        // Если диалоги заблокированы - перекидываем на конкретные страницы разрешений
        if (locationDialogBlocked) {
            println("Location permission dialog blocked, opening location settings")
            openLocationSettings()
            return
        }
        
        if (notificationDialogBlocked) {
            println("Notification permission dialog blocked, opening notification settings")
            openNotificationSettings()
            return
        }

        // Запрашиваем разрешения поочередно в зависимости от версии API
        if (!hasLocationPermissions()) {
            // 1. Всегда запрашиваем основные разрешения на геолокацию
            requestLocationPermissions()
        } else if (!hasBackgroundLocationPermission()) {
            // 2. Запрашиваем фоновое разрешение (только для API 29+)
            requestBackgroundLocationPermission()
        } else if (!hasNotificationPermission()) {
            // 3. Запрашиваем разрешение на уведомления (только для API 33+)
            requestNotificationPermission()
        } else if (!isBatteryOptimizationDisabled()) {
            // 4. Запрашиваем отключение оптимизации батареи (для всех версий)
            println("Requesting battery optimization disable")
            requestBatteryOptimizationDisable()
        } else {
            println("All permissions already granted")
        }
    }

    fun continuePermissionRequest() {
        println("AndroidPermissionRequester.continuePermissionRequest() called")

        // Продолжаем с того места, где остановились
        if (!hasLocationPermissions()) {
            // Если основные разрешения не получены, начинаем сначала
            requestAllPermissions()
        } else if (!hasBackgroundLocationPermission()) {
            // Если основные разрешения есть, но фоновое нет - запрашиваем фоновое
            requestBackgroundLocationPermission()
        } else if (!hasNotificationPermission()) {
            // Если основные разрешения есть, но уведомления нет - запрашиваем уведомления
            requestNotificationPermission()
        } else if (!isBatteryOptimizationDisabled()) {
            // Если все разрешения есть, но оптимизация батареи включена - запрашиваем отключение
            requestBatteryOptimizationDisable()
        } else {
            // Все разрешения получены
            println("All permissions granted")
            onPermissionResult?.invoke(true)
        }
    }

    fun handlePermissionResult(requestCode: Int, grantResults: IntArray) {
        println("AndroidPermissionRequester.handlePermissionResult() called with requestCode: $requestCode")

        when (requestCode) {
            MainActivity.LOCATION_PERMISSION_REQUEST_CODE -> {
                val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                println("Location permissions result: $allGranted")

                if (allGranted) {
                    // Основные разрешения получены, продолжаем с фоновым разрешением
                    continuePermissionRequest()
                } else {
                    // Пользователь отказал в разрешениях - останавливаем процесс
                    println("Location permissions denied, stopping permission request process")
                    onPermissionResult?.invoke(false)
                }
            }

            MainActivity.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> {
                val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                println("Background location permission result: $granted")

                if (granted) {
                    // Фоновое разрешение получено, продолжаем с уведомлениями
                    continuePermissionRequest()
                } else {
                    // Пользователь отказал в фоновом разрешении - останавливаем процесс
                    println("Background location permission denied, stopping permission request process")
                    onPermissionResult?.invoke(false)
                }
            }

            MainActivity.NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                println("Notification permission result: $granted")

                if (granted) {
                    // Уведомления получены, продолжаем с оптимизацией батареи
                    continuePermissionRequest()
                } else {
                    // Пользователь отказал в уведомлениях - останавливаем процесс
                    println("Notification permission denied, stopping permission request process")
                    onPermissionResult?.invoke(false)
                }
            }
        }
    }

    fun requestBatteryOptimizationDisable() {
        println("requestBatteryOptimizationDisable() called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context is androidx.activity.ComponentActivity) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                    println("Battery optimization request sent")
                } catch (e: Exception) {
                    println("Error requesting battery optimization disable: ${e.message}")
                    // Fallback to battery optimization settings page
                    openBatteryOptimizationSettings()
                }
            } else {
                println("Context is not ComponentActivity: ${context.javaClass}")
                openBatteryOptimizationSettings()
            }
        } else {
            println("Battery optimization not required for this Android version")
        }
    }

    fun openLocationSettings() {
        println("openLocationSettings() called")
        try {
            // Попробуем открыть страницу разрешений приложения
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            println("Location settings opened successfully")
        } catch (e: Exception) {
            println("Error opening location settings: ${e.message}")
            // Fallback to general app settings
            openAppSettings()
        }
    }
    
    fun openNotificationSettings() {
        println("openNotificationSettings() called")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
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
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
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
