package com.shiplocate.domain.repository

import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun sendTokenToServer(token: String)

    suspend fun getCachedToken(): String?

    suspend fun sendCachedTokenOnAuth()

    suspend fun clearToken()

    suspend fun startTokenUpdates()

    fun observeTokenUpdates(): Flow<String>

    suspend fun getCurrentTokenFromFirebase(): String?

    suspend fun saveToken(token: String)

    /**
     * Наблюдать за получением push-уведомлений.
     * Возвращает Flow<Int?>, где значение — это type из payload (если его удалось распарсить).
     */
    fun observeReceivedPushes(): Flow<Int?>

    /**
     * Обработка нового токена (вызывается системой Firebase)
     */
    suspend fun onNewTokenReceived(token: String)

    /**
     * Уведомить о получении push-уведомления
     * Вызывается когда приложение запущено и получает push
     *
     * @param type значение поля type из payload (может быть null, если не удалось распарсить)
     */
    suspend fun pushReceived(type: Int?)
}
