package com.tracker.data.datasource.impl

import com.tracker.data.datasource.TrackingDataSource
import com.tracker.data.model.TrackingDataStatus
import com.tracker.domain.datasource.TrackingRequester
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * iOS реализация TrackingDataSource
 * Stateless - только делегирует вызовы в TrackingRequester
 */
class IOSTrackingDataSource : TrackingDataSource, KoinComponent {

    private val trackingRequester: TrackingRequester by inject()

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
