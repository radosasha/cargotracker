package com.shiplocate.data.datasource.impl

import com.shiplocate.data.datasource.DeviceDataSource

/**
 * iOS реализация DeviceDataSource (заглушка)
 */
class IOSDeviceDataSource : DeviceDataSource {
    override suspend fun getBatteryLevel(): Float? {
        // TODO: Реализовать получение уровня батареи для iOS
        return null
    }

    override suspend fun isCharging(): Boolean {
        // TODO: Реализовать проверку статуса зарядки для iOS
        return false
    }
}
