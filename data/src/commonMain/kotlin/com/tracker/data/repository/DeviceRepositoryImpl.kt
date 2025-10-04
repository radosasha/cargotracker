package com.tracker.data.repository

import com.tracker.data.datasource.DeviceDataSource
import com.tracker.domain.repository.DeviceRepository

/**
 * Реализация DeviceRepository в data слое
 */
class DeviceRepositoryImpl(
    private val deviceDataSource: DeviceDataSource
) : DeviceRepository {
    
    override suspend fun getBatteryLevel(): Float? {
        return deviceDataSource.getBatteryLevel()
    }
    
    override suspend fun isCharging(): Boolean {
        return deviceDataSource.isCharging()
    }
}
