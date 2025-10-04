package com.tracker.data.datasource.impl

import com.tracker.data.datasource.GpsLocationDataSource
import com.tracker.data.datasource.GpsManager
import com.tracker.data.model.GpsLocation
import kotlinx.coroutines.flow.Flow

/**
 * Реализация GpsLocationDataSource
 * Делегирует все операции в GpsManager
 */
class GpsLocationDataSourceImpl(
    private val gpsManager: GpsManager
) : GpsLocationDataSource {
    
    override fun startGpsTracking(): Flow<GpsLocation> {
        // Возвращаем поток координат (GPS трекинг запускается автоматически при подписке)
        return gpsManager.observeGpsLocations()
    }
    
    override suspend fun stopGpsTracking(): Result<Unit> {
        return gpsManager.stopGpsTracking()
    }
}

