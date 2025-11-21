package com.shiplocate.data.datasource

import kotlinx.coroutines.flow.Flow

interface FirebaseTokenRemoteDataSource {
    suspend fun sendTokenToServer(token: String)

    suspend fun clearToken()

    /**
     * Уведомить о получении push-уведомления
     * Вызывается когда приложение запущено и получает push
     */
    suspend fun pushReceived()

    /**
     * Наблюдать за получением push-уведомлений
     * Возвращает Flow<Unit> который эмитит Unit при получении push
     */
    fun observeReceivedPushes(): Flow<Unit>
}
