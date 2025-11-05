package com.shiplocate

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat

class AndroidPermissionChecker(private val context: Context) {

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
}
