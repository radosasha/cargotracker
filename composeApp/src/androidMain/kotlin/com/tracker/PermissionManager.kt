package com.tracker

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

class PermissionManager(private val activity: Activity) {
    
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002
        const val BATTERY_OPTIMIZATION_REQUEST_CODE = 1003
        
        // Все необходимые разрешения для GPS трекинга
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        val BACKGROUND_LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }
    
    /**
     * Проверяет, есть ли все необходимые разрешения для GPS трекинга
     */
    fun hasAllLocationPermissions(): Boolean {
        return LOCATION_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Проверяет, есть ли разрешение на фоновое отслеживание местоположения
     */
    fun hasBackgroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                activity, 
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Для старых версий Android не требуется
        }
    }
    
    /**
     * Проверяет, есть ли разрешение на уведомления
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Для старых версий Android не требуется
        }
    }
    
    /**
     * Проверяет, отключена ли оптимизация батареи для приложения
     */
    fun isBatteryOptimizationDisabled(): Boolean {
        val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(activity.packageName)
    }
    
    /**
     * Запрашивает основные разрешения на местоположение
     */
    fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            activity,
            LOCATION_PERMISSIONS,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
    
    /**
     * Запрашивает разрешение на фоновое отслеживание местоположения
     */
    fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                activity,
                BACKGROUND_LOCATION_PERMISSIONS,
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }
    
    /**
     * Запрашивает разрешение на уведомления
     */
    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }
    
    /**
     * Открывает настройки для отключения оптимизации батареи
     */
    fun requestBatteryOptimizationDisable() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${activity.packageName}")
        }
        activity.startActivityForResult(intent, BATTERY_OPTIMIZATION_REQUEST_CODE)
    }
    
    /**
     * Открывает настройки приложения
     */
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${activity.packageName}")
        }
        activity.startActivity(intent)
    }
    
    /**
     * Проверяет, нужно ли показать объяснение для разрешения
     */
    fun shouldShowRationale(permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
    
    /**
     * Получает список всех отсутствующих разрешений
     */
    fun getMissingPermissions(): List<String> {
        val missingPermissions = mutableListOf<String>()
        
        LOCATION_PERMISSIONS.forEach { permission ->
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission)
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    activity, 
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity, 
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        return missingPermissions
    }
}
