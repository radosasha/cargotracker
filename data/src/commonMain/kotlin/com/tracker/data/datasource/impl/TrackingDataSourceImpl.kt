package com.tracker.data.datasource.impl

import com.tracker.data.datasource.TrackingDataSource
import com.tracker.data.model.TrackingDataStatus
import com.tracker.data.datasource.TrackingRequester

/**
 * Общая реализация TrackingDataSource для всех платформ
 * Stateless - только делегирует вызовы в TrackingRequester
 */
class TrackingDataSourceImpl(
    private val trackingRequester: TrackingRequester
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
