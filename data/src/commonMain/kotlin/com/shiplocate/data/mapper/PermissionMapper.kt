package com.shiplocate.data.mapper

import com.shiplocate.data.model.PermissionDataModel
import com.shiplocate.domain.model.PermissionStatus

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
//            hasActivityRecognitionPermission = dataModel.hasActivityRecognitionPermission,
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
//            hasActivityRecognitionPermission = domainModel.hasActivityRecognitionPermission,
            isBatteryOptimizationDisabled = domainModel.isBatteryOptimizationDisabled,
        )
    }
}
