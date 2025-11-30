package com.shiplocate.data.datasource

import kotlinx.coroutines.flow.Flow

interface FirebaseTokenRemoteDataSource {
    suspend fun sendTokenToServer(token: String)

    suspend fun clearToken()

    /**
     * Уведомить о получении push-уведомления
     * Вызывается когда приложение запущено и получает push
     *
     * @param type значение поля type из payload (может быть null)
     */
    suspend fun pushReceived(type: Int?)

    /**
     * Наблюдать за получением push-уведомлений
     * Возвращает Flow<Int?> который эмитит type при получении push
     */
    fun observeReceivedPushes(): Flow<Int?>
}
