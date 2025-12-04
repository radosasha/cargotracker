package com.shiplocate.domain.usecase

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.load.LoadStatus
import com.shiplocate.domain.repository.AuthRepository
import com.shiplocate.domain.repository.LoadRepository
import com.shiplocate.domain.repository.TrackingRepository
import com.shiplocate.domain.usecase.load.BaseLoadsUseCase

/**
 * Use Case для обработки push-уведомлений когда приложение не запущено
 * Вызывает LoadRepository.getLoads(token) для обновления данных
 */
class HandlePushNotificationWhenAppKilledUseCase(
    private val loadRepository: LoadRepository,
    private val trackingRepository: TrackingRepository,
    authRepository: AuthRepository,
    private val logger: Logger,
) : BaseLoadsUseCase(loadRepository, authRepository, logger) {
    /**
     * Обрабатывает push-уведомление когда приложение не запущено
     * Обновляет список loads с сервера
     */
    suspend operator fun invoke(): Result<Unit> {
        return try {
            logger.info(LogCategory.GENERAL, "HandlePushNotificationWhenAppKilledUseCase: Processing push notification when app was killed")

            // Вызываем LoadRepository.getLoads для обновления данных
            val result = getLoads()

            val connectedLoad = loadRepository.getConnectedLoad()

            result.fold(
                onSuccess = { loads ->
                    logger.info(
                        LogCategory.GENERAL,
                        "HandlePushNotificationWhenAppKilledUseCase: Successfully refreshed ${loads.size} loads"
                    )
                    if (connectedLoad != null) {
                        val stillInActive = loads.any { it.id == connectedLoad.id && it.loadStatus == LoadStatus.LOAD_STATUS_CONNECTED }
                        if (!stillInActive) {
                            logger.info(
                                LogCategory.GENERAL,
                                "HandlePushNotificationWhenAppKilledUseCase: Load was IN_TRANSIT, but now it's not, Stop tracking"
                            )
                            trackingRepository.stopTracking()
                        }
                    }
                    Result.success(Unit)
                },
                onFailure = { error ->
                    logger.error(
                        LogCategory.GENERAL,
                        "HandlePushNotificationWhenAppKilledUseCase: Failed to refresh loads: ${error.message}"
                    )
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            logger.error(LogCategory.GENERAL, "HandlePushNotificationWhenAppKilledUseCase: Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
}

