package com.shiplocate.data.datasource.impl

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import com.shiplocate.data.datasource.DeviceDataSource

/**
 * Android-специфичная реализация DeviceDataSource
 * Получает информацию об устройстве через Android системные сервисы
 */
class AndroidDeviceDataSource(
    private val context: Context,
) : DeviceDataSource {
    override suspend fun getBatteryLevel(): Float? {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val batteryLevel = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.toFloat()
            println("AndroidDeviceDataSource: Battery level: $batteryLevel%")
            batteryLevel
        } catch (e: Exception) {
            println("AndroidDeviceDataSource: Error getting battery level: ${e.message}")
            null
        }
    }

    override suspend fun isCharging(): Boolean {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val isCharging = batteryManager?.isCharging ?: false
            println("AndroidDeviceDataSource: Is charging: $isCharging")
            isCharging
        } catch (e: Exception) {
            println("AndroidDeviceDataSource: Error checking charging status: ${e.message}")
            false
        }
    }

    override suspend fun getPlatform(): String {
        return "Android"
    }

    override suspend fun getOsVersion(): String {
        return try {
            val version = Build.VERSION.RELEASE
            println("AndroidDeviceDataSource: OS version: $version")
            version
        } catch (e: Exception) {
            println("AndroidDeviceDataSource: Error getting OS version: ${e.message}")
            "Unknown"
        }
    }

    override suspend fun getDeviceModel(): String {
        return try {
            val model = Build.MODEL
            println("AndroidDeviceDataSource: Device model: $model")
            model
        } catch (e: Exception) {
            println("AndroidDeviceDataSource: Error getting device model: ${e.message}")
            "Unknown"
        }
    }
}
