package com.shiplocate.data.datasource.impl

import com.shiplocate.data.datasource.GpsLocationDataSource
import com.shiplocate.data.datasource.GpsManager
import com.shiplocate.data.model.GpsLocation
import kotlinx.coroutines.flow.Flow

/**
 * Реализация GpsLocationDataSource
 * Делегирует все операции в GpsManager
 */
class GpsLocationDataSourceImpl(
    private val gpsManager: GpsManager,
) : GpsLocationDataSource {
    override suspend fun startGpsTracking(): Flow<GpsLocation> {
        // Возвращаем поток координат (GPS трекинг запускается автоматически при подписке)
        return gpsManager.startGpsTracking()
    }

    override suspend fun stopGpsTracking(): Result<Unit> {
        return gpsManager.stopGpsTracking()
    }
}
