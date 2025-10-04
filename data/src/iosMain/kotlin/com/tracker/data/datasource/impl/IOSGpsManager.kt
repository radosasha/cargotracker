package com.tracker.data.datasource.impl

import com.tracker.data.datasource.GpsManager
import com.tracker.data.model.GpsLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock

/**
 * iOS реализация GpsManager (заглушка)
 */
class IOSGpsManager : GpsManager {
    override suspend fun startGpsTracking(): Result<Unit> {
        // TODO: Реализовать GPS трекинг для iOS
        return Result.success(Unit)
    }

    override suspend fun stopGpsTracking(): Result<Unit> {
        // TODO: Реализовать остановку GPS трекинга для iOS
        return Result.success(Unit)
    }

    override fun isGpsTrackingActive(): Boolean {
        // TODO: Реализовать проверку статуса GPS трекинга для iOS
        return false
    }

    override fun observeGpsLocations(): Flow<GpsLocation> {
        // TODO: Реализовать поток GPS координат для iOS
        return flowOf()
    }
}
