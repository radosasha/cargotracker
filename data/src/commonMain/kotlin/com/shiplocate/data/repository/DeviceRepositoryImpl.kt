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

    override suspend fun getPlatform(): String {
        return deviceDataSource.getPlatform()
    }

    override suspend fun getOsVersion(): String {
        return deviceDataSource.getOsVersion()
    }

    override suspend fun getDeviceModel(): String {
        return deviceDataSource.getDeviceModel()
    }

    override suspend fun getDeviceInfo(): String {
        val platform = getPlatform()
        val osVersion = getOsVersion()
        val deviceModel = getDeviceModel()
        return "$platform/$osVersion/$deviceModel"
    }

    override suspend fun getApiLevel(): Int {
        return deviceDataSource.getApiLevel()
    }
}
