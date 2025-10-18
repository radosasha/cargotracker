package com.shiplocate.data.repository

import com.shiplocate.data.datasource.DeviceDataSource
import com.shiplocate.domain.repository.DeviceRepository

/**
 * Реализация DeviceRepository в data слое
 */
class DeviceRepositoryImpl(
    private val deviceDataSource: DeviceDataSource,
) : DeviceRepository {
    override suspend fun getBatteryLevel(): Float? {
        return deviceDataSource.getBatteryLevel()
    }

    override suspend fun isCharging(): Boolean {
        return deviceDataSource.isCharging()
    }
}
