package com.shiplocate.domain.usecase

import com.shiplocate.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use Case для наблюдения за получением push-уведомлений
 * Возвращает Flow<Unit> который эмитит Unit при получении push
 */
class ObserveReceivedPushesUseCase(
    private val notificationRepository: NotificationRepository,
) {
    /**
     * Наблюдать за получением push-уведомлений
     * @return Flow<Int?> который эмитит type при получении push
     */
    operator fun invoke(): Flow<Int?> {
        return notificationRepository.observeReceivedPushes()
    }
}

