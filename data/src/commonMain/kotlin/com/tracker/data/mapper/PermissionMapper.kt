package com.tracker.data.mapper

import com.tracker.data.model.PermissionDataModel
import com.tracker.domain.model.PermissionStatus

/**
 * Маппер для преобразования между Data и Domain моделями Permission
 */
object PermissionMapper {
    /**
     * Преобразует Data модель в Domain модель
     */
    fun toDomain(dataModel: PermissionDataModel): PermissionStatus {
        return PermissionStatus(
            hasLocationPermission = dataModel.hasLocationPermission,
            hasBackgroundLocationPermission = dataModel.hasBackgroundLocationPermission,
            hasNotificationPermission = dataModel.hasNotificationPermission,
            isBatteryOptimizationDisabled = dataModel.isBatteryOptimizationDisabled,
        )
    }

    /**
     * Преобразует Domain модель в Data модель
     */
    fun toData(domainModel: PermissionStatus): PermissionDataModel {
        return PermissionDataModel(
            hasLocationPermission = domainModel.hasLocationPermission,
            hasBackgroundLocationPermission = domainModel.hasBackgroundLocationPermission,
            hasNotificationPermission = domainModel.hasNotificationPermission,
            isBatteryOptimizationDisabled = domainModel.isBatteryOptimizationDisabled,
        )
    }
}
