package com.shiplocate.data.datasource.impl

import com.shiplocate.data.datasource.TrackingDataSource
import com.shiplocate.data.model.TrackingDataStatus
import com.shiplocate.trackingsdk.TrackingSDK

/**
 * Общая реализация TrackingDataSource для всех платформ
 * Получает TrackingSDK через DI конструктор
 */
class TrackingDataSourceImpl(
    private val trackingSDK: TrackingSDK
) : TrackingDataSource {
    
    override suspend fun startTracking(): Result<Unit> {
        return trackingSDK.startTracking()
    }

    override suspend fun stopTracking(): Result<Unit> {
        return trackingSDK.stopTracking()
    }

    override suspend fun getTrackingStatus(): TrackingDataStatus {
        return if (trackingSDK.isTrackingActive()) {
            TrackingDataStatus.ACTIVE
        } else {
            TrackingDataStatus.STOPPED
        }
    }
}
