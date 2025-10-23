package com.shiplocate.data.mapper

import com.shiplocate.data.model.TrackingDataStatus
import com.shiplocate.domain.model.TrackingStatus

/**
 * Маппер для преобразования между Data и Domain моделями Tracking
 */
object TrackingMapper {
    /**
     * Преобразует Data модель в Domain модель
     */
    fun toDomain(dataStatus: TrackingDataStatus): TrackingStatus {
        return when (dataStatus) {
            TrackingDataStatus.STOPPED -> TrackingStatus.STOPPED
            TrackingDataStatus.STARTING -> TrackingStatus.STARTING
            TrackingDataStatus.ACTIVE -> TrackingStatus.ACTIVE
            TrackingDataStatus.STOPPING -> TrackingStatus.STOPPING
            TrackingDataStatus.ERROR -> TrackingStatus.ERROR
        }
    }

    /**
     * Преобразует Domain модель в Data модель
     */
    fun toData(domainStatus: TrackingStatus): TrackingDataStatus {
        return when (domainStatus) {
            TrackingStatus.STOPPED -> TrackingDataStatus.STOPPED
            TrackingStatus.STARTING -> TrackingDataStatus.STARTING
            TrackingStatus.ACTIVE -> TrackingDataStatus.ACTIVE
            TrackingStatus.STOPPING -> TrackingDataStatus.STOPPING
            TrackingStatus.ERROR -> TrackingDataStatus.ERROR
        }
    }
}
