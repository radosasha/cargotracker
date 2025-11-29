package com.shiplocate

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

/**
 * Android класс для запроса разрешений
 * Использует только Android API для определения состояния разрешений,
 * без использования SharedPreferences
 */
class AndroidPermissionRequester(private val context: Activity) {

    fun shouldShowLocationPermissionRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun shouldShowActivityRecognitionPermissionRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION,
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun shouldShowBackgroundLocationPermissionRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
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
        println("Requesting location permissions")
        ActivityCompat.requestPermissions(
            context,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
            MainActivity.LOCATION_PERMISSION_REQUEST_CODE,
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestBackgroundLocationPermission() {
        println("Requesting background location permission")
        ActivityCompat.requestPermissions(
            context,
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            MainActivity.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE,
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestNotificationPermission() {
        ActivityCompat.requestPermissions(
            context,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            MainActivity.REQUEST_NOTIFICATIONS_PERMISSION,
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestActivityRecognitionPermission() {
        println("Requesting activity recognition permission")
        ActivityCompat.requestPermissions(
            context,
            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
            MainActivity.ACTIVITY_RECOGNITION_PERMISSION_REQUEST_CODE,
        )
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
            val intent =
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            println("Notification settings opened successfully")
        } catch (e: Exception) {
            println("Error opening notification settings: ${e.message}")
            // Fallback to general app settings
            openAppSettings()
        }
    }

    fun openBatteryOptimizationSettings() {
        println("openBatteryOptimizationSettings() called")
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            println("Battery optimization settings opened successfully")
        } catch (e: Exception) {
            println("Error opening battery optimization settings: ${e.message}")
            // Fallback to general app settings
            openAppSettings()
        }
    }

    fun openAirplaneModeSettings() {
        println("openAirplaneModeSettings() called")
        try {
            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            println("Airplane mode settings opened successfully")
        } catch (e: Exception) {
            println("Error opening airplane mode settings: ${e.message}")
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
