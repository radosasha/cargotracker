package com.shiplocate.domain.usecase

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.load.LoadStatus
import com.shiplocate.domain.repository.AuthPreferencesRepository
import com.shiplocate.domain.repository.LoadRepository
import com.shiplocate.domain.repository.TrackingRepository

/**
 * Use Case для обработки push-уведомлений когда приложение не запущено
 * Вызывает LoadRepository.getLoads(token) для обновления данных
 */
class HandlePushNotificationWhenAppKilledUseCase(
    private val loadRepository: LoadRepository,
    private val trackingRepository: TrackingRepository,
    private val authPreferencesRepository: AuthPreferencesRepository,
    private val logger: Logger,
) {
    /**
     * Обрабатывает push-уведомление когда приложение не запущено
     * Обновляет список loads с сервера
     */
    suspend operator fun invoke(): Result<Unit> {
        return try {
            logger.info(LogCategory.GENERAL, "HandlePushNotificationWhenAppKilledUseCase: Processing push notification when app was killed")

            // Получаем токен авторизации
            val authSession = authPreferencesRepository.getSession()
            val token = authSession?.token

            if (token == null) {
                logger.warn(LogCategory.GENERAL, "HandlePushNotificationWhenAppKilledUseCase: No auth session found, cannot refresh loads")
                return Result.failure(Exception("Not authenticated"))
            }

            val connectedLoad = loadRepository.getConnectedLoad()

            // Вызываем LoadRepository.getLoads(token) для обновления данных
            val result = loadRepository.getLoads(token)

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

