package com.shiplocate.data.datasource.impl

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.DeviceDataSource

/**
 * Android-специфичная реализация DeviceDataSource
 * Получает информацию об устройстве через Android системные сервисы
 */
class AndroidDeviceDataSource(
    private val context: Context,
    private val logger: Logger,
) : DeviceDataSource {
    override suspend fun getBatteryLevel(): Float? {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val batteryLevel = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.toFloat()
            logger.debug(LogCategory.GENERAL, "AndroidDeviceDataSource: Battery level: $batteryLevel%")
            batteryLevel
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "AndroidDeviceDataSource: Error getting battery level: ${e.message}", e)
            null
        }
    }

    override suspend fun isCharging(): Boolean {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val isCharging = batteryManager?.isCharging ?: false
            logger.debug(LogCategory.GENERAL, "AndroidDeviceDataSource: Is charging: $isCharging")
            isCharging
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "AndroidDeviceDataSource: Error checking charging status: ${e.message}", e)
            false
        }
    }

    override suspend fun getPlatform(): String {
        return "Android"
    }

    override suspend fun getOsVersion(): String {
        return try {
            val version = Build.VERSION.RELEASE
            logger.debug(LogCategory.GENERAL, "AndroidDeviceDataSource: OS version: $version")
            version
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "AndroidDeviceDataSource: Error getting OS version: ${e.message}", e)
            "Unknown"
        }
    }

    override suspend fun getDeviceModel(): String {
        return try {
            val model = Build.MODEL
            logger.debug(LogCategory.GENERAL, "AndroidDeviceDataSource: Device model: $model")
            model
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "AndroidDeviceDataSource: Error getting device model: ${e.message}", e)
            "Unknown"
        }
    }
}
