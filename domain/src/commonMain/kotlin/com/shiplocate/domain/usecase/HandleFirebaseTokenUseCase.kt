package com.shiplocate.domain.usecase

import com.shiplocate.domain.repository.AuthPreferencesRepository
import com.shiplocate.domain.repository.NotificationRepository

/**
 * Use Case для обработки нового Firebase токена
 * Содержит бизнес-логику: отправлять на сервер если авторизован
 */
class HandleFirebaseTokenUseCase(
    private val notificationRepository: NotificationRepository,
    private val authPreferencesRepository: AuthPreferencesRepository,
) {
    suspend operator fun invoke(token: String) {
        // Если пользователь авторизован - отправляем на сервер
        if (authPreferencesRepository.hasSession()) {
            notificationRepository.sendTokenToServer(token)
        }
        // Если не авторизован - токен уже сохранен в DataStore через DataSource
    }
}
