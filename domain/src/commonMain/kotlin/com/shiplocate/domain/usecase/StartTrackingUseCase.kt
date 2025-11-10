package com.shiplocate.domain.usecase

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.TrackingStatus
import com.shiplocate.domain.repository.PermissionRepository
import com.shiplocate.domain.repository.PrefsRepository
import com.shiplocate.domain.repository.TrackingRepository

/**
 * Use Case для запуска GPS трекинга
 * Сохраняет состояние в DataStore при успешном запуске
 */
class StartTrackingUseCase(
    private val permissionRepository: PermissionRepository,
    private val trackingRepository: TrackingRepository,
    private val prefsRepository: PrefsRepository,
    private val logger: Logger,
) {
    suspend operator fun invoke(loadId: Long): Result<Unit> {
        // Проверяем разрешения перед запуском
        val permissionStatus = permissionRepository.getPermissionStatus()

        return if (permissionStatus.hasAllPermissions) {
            // Проверяем, не активен ли уже трекинг
            val currentStatus = trackingRepository.getTrackingStatus()
            if (currentStatus == TrackingStatus.ACTIVE) {
                logger.info(LogCategory.LOCATION, "StartTrackingUseCase: Tracking is already active, no need to start")
                // Убеждаемся, что состояние в DataStore корректное
                prefsRepository.saveTrackingState(true)
                return Result.success(Unit)
            }

            val result = trackingRepository.startTracking(loadId)

            result
        } else {
            Result.failure(
                IllegalStateException("Не все необходимые разрешения получены"),
            )
        }
    }
}
