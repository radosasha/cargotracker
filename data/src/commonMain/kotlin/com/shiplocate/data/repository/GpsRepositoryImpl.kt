package com.shiplocate.data.repository

import com.shiplocate.data.datasource.GpsLocationDataSource
import com.shiplocate.data.mapper.GpsLocationMapper
import com.shiplocate.domain.model.GpsLocation
import com.shiplocate.domain.repository.GpsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GpsRepositoryImpl(
    private val gpsLocationDataSource: GpsLocationDataSource,
): GpsRepository {

    override suspend fun startGpsTracking(): Flow<GpsLocation> {
        return gpsLocationDataSource.startGpsTracking().map { gpsLocation ->
            GpsLocationMapper.toDomain(gpsLocation)
        }
    }

    override suspend fun stopGpsTracking(): Result<Unit> {
        return gpsLocationDataSource.stopGpsTracking()
    }
}
