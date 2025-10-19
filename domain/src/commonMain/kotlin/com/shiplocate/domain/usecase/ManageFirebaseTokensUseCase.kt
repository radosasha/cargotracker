package com.shiplocate.domain.usecase

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
) {
    private val coroutineScope = MainScope()

    suspend fun startManaging() {
        println("ManageFirebaseTokensUseCase: Starting Firebase token management")

        // 1. Запускаем прослушивание новых токенов от Firebase
        notificationRepository.startTokenUpdates()

        // 2. Получаем текущий токен напрямую от Firebase (решает проблему с задержкой onNewToken)
        val currentToken = notificationRepository.getCurrentTokenFromFirebase()
        if (currentToken != null) {
            println("ManageFirebaseTokensUseCase: Got current token from Firebase: ${currentToken.take(20)}...")

            // Кешируем токен
            // Если пользователь авторизован - отправляем на сервер
            if (authPreferencesRepository.hasSession()) {
                println("ManageFirebaseTokensUseCase: User is authenticated, sending current token to server")
                notificationRepository.sendTokenToServer(currentToken)
            } else {
                println("ManageFirebaseTokensUseCase: User not authenticated, current token will be cached by startTokenUpdates")
            }
        } else {
            println("ManageFirebaseTokensUseCase: No current token available from Firebase")
        }

        // 3. Слушаем изменения в локальном кеше токенов и отправляем на сервер при авторизации
        notificationRepository.observeTokenUpdates()
            .onEach { token ->
                if (token.isNotEmpty()) {
                    println("ManageFirebaseTokensUseCase: Token updated in local cache: ${token.take(20)}...")

                    // Если пользователь авторизован - отправляем на сервер
                    if (authPreferencesRepository.hasSession()) {
                        println("ManageFirebaseTokensUseCase: User is authenticated, sending token to server")
                        notificationRepository.sendTokenToServer(token)
                    } else {
                        println("ManageFirebaseTokensUseCase: User not authenticated, token cached for later")
                    }
                }
            }
            .launchIn(coroutineScope)
    }
}
