package com.shiplocate.domain.usecase

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.repository.PrefsRepository
import com.shiplocate.domain.repository.TrackingRepository
import com.shiplocate.domain.service.LocationSyncService

/**
 * Use Case для остановки GPS трекинга
 * Сохраняет состояние в DataStore при успешной остановке
 */
class StopTrackingUseCase(
    private val trackingRepository: TrackingRepository,
    private val prefsRepository: PrefsRepository,
    private val locationSyncManager: LocationSyncService,
    private val logger: Logger,
) {
    suspend operator fun invoke(): Result<Unit> {
        // Проверяем, не остановлен ли уже трекинг
        val currentStatus = trackingRepository.getTrackingStatus()
        if (currentStatus == com.shiplocate.domain.model.TrackingStatus.STOPPED) {
            logger.info(LogCategory.LOCATION, "StopTrackingUseCase: Tracking is already stopped, no need to stop")
            // Убеждаемся, что состояние в DataStore корректное
            prefsRepository.saveTrackingState(false)
            return Result.success(Unit)
        }

        // Останавливаем синхронизацию
        locationSyncManager.stopSync()
        logger.info(LogCategory.LOCATION, "StopTrackingUseCase: Location sync stopped")

        // Останавливаем трекинг
        val result = trackingRepository.stopTracking()

        // Если трекинг успешно остановлен, сохраняем состояние в DataStore
        if (result.isSuccess) {
            prefsRepository.saveTrackingState(false)
            logger.info(LogCategory.LOCATION, "StopTrackingUseCase: Tracking stopped and state saved to DataStore")
        }

        return result
    }
}
