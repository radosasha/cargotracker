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
}
