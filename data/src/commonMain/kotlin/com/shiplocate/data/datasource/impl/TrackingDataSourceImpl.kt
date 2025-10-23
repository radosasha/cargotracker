package com.shiplocate.data.datasource.impl

import com.shiplocate.data.datasource.TrackingDataSource
import com.shiplocate.data.datasource.TrackingRequester
import com.shiplocate.data.model.TrackingDataStatus

/**
 * Общая реализация TrackingDataSource для всех платформ
 * Stateless - только делегирует вызовы в TrackingRequester
 */
class TrackingDataSourceImpl(
    private val trackingRequester: TrackingRequester,
) : TrackingDataSource {
    override suspend fun startTracking(): Result<Unit> {
        return trackingRequester.startTracking()
    }

    override suspend fun stopTracking(): Result<Unit> {
        return trackingRequester.stopTracking()
    }

    override suspend fun getTrackingStatus(): TrackingDataStatus {
        return if (trackingRequester.isTrackingActive()) {
            TrackingDataStatus.ACTIVE
        } else {
            TrackingDataStatus.STOPPED
        }
    }
}
