package com.shiplocate.domain.usecase

import com.shiplocate.domain.repository.AuthRepository
import com.shiplocate.domain.repository.NotificationRepository

/**
 * Use Case для обработки нового Firebase токена
 * Содержит бизнес-логику: отправлять на сервер если авторизован
 */
class HandleFirebaseTokenUseCase(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(token: String) {
        // Если пользователь авторизован - отправляем на сервер
        if (authRepository.hasSession()) {
            notificationRepository.sendTokenToServer(token)
        }
        // Если не авторизован - токен уже сохранен в DataStore через DataSource
    }
}
