package com.shiplocate.data.datasource.impl

import com.shiplocate.data.datasource.DeviceDataSource
import platform.UIKit.UIDevice
import platform.Foundation.NSProcessInfo

/**
 * iOS реализация DeviceDataSource
 * Получает информацию об устройстве через iOS системные API
 */
class IOSDeviceDataSource : DeviceDataSource {
    override suspend fun getBatteryLevel(): Float? {
        // TODO: Реализовать получение уровня батареи для iOS
        // Требует:
        // 1. Включить battery monitoring: UIDevice.currentDevice.isBatteryMonitoringEnabled = true
        // 2. Получить уровень: UIDevice.currentDevice.batteryLevel
        // 3. Добавить разрешения в Info.plist если необходимо
        return null
    }

    override suspend fun isCharging(): Boolean {
        // TODO: Реализовать проверку статуса зарядки для iOS
        // Требует:
        // 1. Включить battery monitoring: UIDevice.currentDevice.isBatteryMonitoringEnabled = true
        // 2. Проверить статус: UIDevice.currentDevice.batteryState
        // 3. Добавить разрешения в Info.plist если необходимо
        return false
    }

    override suspend fun getPlatform(): String {
        return "iOS"
    }

    override suspend fun getOsVersion(): String {
        return try {
            val device = UIDevice.currentDevice
            val systemVersion = device.systemVersion
            val systemName = device.systemName
            
            // Получаем дополнительную информацию через NSProcessInfo
            val processInfo = NSProcessInfo.processInfo
            val operatingSystemVersion = processInfo.operatingSystemVersionString
            
            val fullVersion = "$systemName $systemVersion ($operatingSystemVersion)"
            println("IOSDeviceDataSource: OS version: $fullVersion")
            fullVersion
        } catch (e: Exception) {
            println("IOSDeviceDataSource: Error getting OS version: ${e.message}")
            "Unknown"
        }
    }

    override suspend fun getDeviceModel(): String {
        return try {
            val device = UIDevice.currentDevice
            val model = device.model
            val name = device.name
            val systemName = device.systemName
            
            // Получаем дополнительную информацию через NSProcessInfo
            val processInfo = NSProcessInfo.processInfo
            val hostName = processInfo.hostName
            val operatingSystemVersion = processInfo.operatingSystemVersionString
            
            val deviceInfo = "$model ($name) - $systemName $operatingSystemVersion"
            println("IOSDeviceDataSource: Device model: $deviceInfo")
            deviceInfo
        } catch (e: Exception) {
            println("IOSDeviceDataSource: Error getting device model: ${e.message}")
            "Unknown"
        }
    }
}
