package com.shiplocate.domain.usecase

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.repository.AuthPreferencesRepository
import com.shiplocate.domain.repository.NotificationRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Use Case для управления Firebase токенами
 * Запускает прослушивание новых токенов и отправляет их на сервер при авторизации
 */
class ManageFirebaseTokensUseCase(
    private val notificationRepository: NotificationRepository,
    private val authPreferencesRepository: AuthPreferencesRepository,
    private val logger: Logger,
) {
    private val coroutineScope = MainScope()

    suspend fun startManaging() {
        logger.info(LogCategory.GENERAL, "ManageFirebaseTokensUseCase: Starting Firebase token management")

        // 1. Запускаем прослушивание новых токенов от Firebase и сохранение локально
        notificationRepository.startTokenUpdates()

        // 2. Получаем текущий токен напрямую от Firebase (решает проблему с задержкой onNewToken)
        val currentToken = notificationRepository.getCurrentTokenFromFirebase()
        if (currentToken != null) {
            logger.info(LogCategory.GENERAL, "ManageFirebaseTokensUseCase: Got token from Firebase on-demand: ${currentToken.take(20)}...")

            // Кешируем токен
            if (notificationRepository.getCachedToken() == null) {
                logger.info(
                    LogCategory.GENERAL,
                    "ManageFirebaseTokensUseCase: Caching demanded token from Firebase: ${currentToken.take(20)}...",
                )
                notificationRepository.saveToken(currentToken)
            }

            // Если пользователь авторизован - отправляем на сервер
            if (authPreferencesRepository.hasSession()) {
                logger.info(LogCategory.GENERAL, "ManageFirebaseTokensUseCase: User is authenticated, sending current token to server")
                try {
                    notificationRepository.sendTokenToServer(currentToken)
                } catch (e: Exception) {
                    logger.error(LogCategory.NETWORK, "ManageFirebaseTokensUseCase:(case#1) Error sending firebase token: ${e.message}", e)
                }
            } else {
                logger.info(
                    LogCategory.GENERAL,
                    "ManageFirebaseTokensUseCase: User not authenticated, current token will be cached by startTokenUpdates",
                )
            }
        } else {
            logger.info(LogCategory.GENERAL, "ManageFirebaseTokensUseCase: No current token available from Firebase")
        }

        // 3. Слушаем изменения в локальном кеше токенов и отправляем на сервер при авторизации
        notificationRepository.observeTokenUpdates()
            .onEach { token ->
                if (token.isNotEmpty()) {
                    logger.info(LogCategory.GENERAL, "ManageFirebaseTokensUseCase: Token updated in local cache: ${token.take(20)}...")

                    // Если пользователь авторизован - отправляем на сервер
                    if (authPreferencesRepository.hasSession()) {
                        logger.info(LogCategory.GENERAL, "ManageFirebaseTokensUseCase: User is authenticated, sending token to server")
                        try {
                            notificationRepository.sendTokenToServer(token)
                        } catch (e: Exception) {
                            logger.error(
                                LogCategory.NETWORK,
                                "ManageFirebaseTokensUseCase:(case#2) Error sending firebase token: ${e.message}",
                                e
                            )
                        }
                    } else {
                        logger.info(LogCategory.GENERAL, "ManageFirebaseTokensUseCase: User not authenticated, token cached for later")
                    }
                }
            }
            .launchIn(coroutineScope)
    }
}
