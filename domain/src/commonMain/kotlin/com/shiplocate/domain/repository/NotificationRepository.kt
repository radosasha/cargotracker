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
     * Наблюдать за получением push-уведомлений
     * Возвращает Flow<Unit> который эмитит Unit при получении push
     */
    fun observeReceivedPushes(): Flow<Unit>

    /**
     * Обработка нового токена (вызывается системой Firebase)
     */
    suspend fun onNewTokenReceived(token: String)

    /**
     * Обработка push-уведомления (вызывается системой Firebase)
     */
    fun onPushNotificationReceived(userInfo: Map<String, Any>)

    /**
     * Уведомить о получении push-уведомления
     * Вызывается когда приложение запущено и получает push
     */
    suspend fun pushReceived()
}
