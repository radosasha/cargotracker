package com.tracker.data.repository

import com.tracker.data.datasource.TrackingDataSource
import com.tracker.data.mapper.TrackingMapper
import com.tracker.domain.model.TrackingStatus
import com.tracker.domain.repository.TrackingRepository

/**
 * Реализация TrackingRepository
 */
class TrackingRepositoryImpl(
    private val trackingDataSource: TrackingDataSource,
) : TrackingRepository {
    override suspend fun startTracking(): Result<Unit> {
        return trackingDataSource.startTracking()
    }

    override suspend fun stopTracking(): Result<Unit> {
        return trackingDataSource.stopTracking()
    }

    override suspend fun getTrackingStatus(): TrackingStatus {
        val dataStatus = trackingDataSource.getTrackingStatus()
        return TrackingMapper.toDomain(dataStatus)
    }
}
