package com.tracker.data.datasource.impl

import android.content.Context
import android.os.BatteryManager
import com.tracker.data.datasource.DeviceDataSource

/**
 * Android-специфичная реализация DeviceDataSource
 * Получает информацию об устройстве через Android системные сервисы
 */
class AndroidDeviceDataSource(
    private val context: Context
) : DeviceDataSource {
    
    override suspend fun getBatteryLevel(): Float? {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val batteryLevel = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.toFloat()
            println("AndroidDeviceDataSource: Battery level: ${batteryLevel}%")
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
}
