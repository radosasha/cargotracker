package com.shiplocate.domain.usecase

import com.shiplocate.domain.repository.NotificationRepository

/**
 * Use Case для отправки кешированного токена при авторизации
 */
class SendCachedTokenOnAuthUseCase(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke() {
        // Получаем кешированный токен и отправляем на сервер
        notificationRepository.sendCachedTokenOnAuth()
    }
}
