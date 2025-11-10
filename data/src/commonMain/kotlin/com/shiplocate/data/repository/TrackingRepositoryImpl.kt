package com.shiplocate.data.repository

import com.shiplocate.data.datasource.TrackingDataSource
import com.shiplocate.data.mapper.TrackingMapper
import com.shiplocate.domain.model.TrackingStatus
import com.shiplocate.domain.repository.TrackingRepository

/**
 * Реализация TrackingRepository
 */
class TrackingRepositoryImpl(
    private val trackingDataSource: TrackingDataSource,
) : TrackingRepository {
    override suspend fun startTracking(loadId: Long): Result<Unit> {
        return trackingDataSource.startTracking(loadId)
    }

    override suspend fun stopTracking(): Result<Unit> {
        return trackingDataSource.stopTracking()
    }

    override suspend fun getTrackingStatus(): TrackingStatus {
        val dataStatus = trackingDataSource.getTrackingStatus()
        return TrackingMapper.toDomain(dataStatus)
    }
}
