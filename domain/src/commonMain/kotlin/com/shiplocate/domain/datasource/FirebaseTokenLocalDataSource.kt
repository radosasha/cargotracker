package com.shiplocate.domain.datasource

import kotlinx.coroutines.flow.Flow

/**
 * Domain интерфейс для локального кеширования Firebase токенов
 * Отвечает за локальное хранение токенов и отслеживание статуса отправки
 */
interface FirebaseTokenLocalDataSource {
    suspend fun saveToken(token: String)

    suspend fun getCachedToken(): String?

    fun observeTokenUpdates(): Flow<String>
    
    /**
     * Пометить токен как отправленный на сервер
     */
    suspend fun markTokenAsSent()
    
    /**
     * Пометить токен как неотправленный на сервер
     */
    suspend fun markTokenAsNotSent()
    
    /**
     * Проверить, был ли токен отправлен на сервер
     */
    suspend fun isTokenSent(): Boolean
    
}
