package com.shiplocate.domain.datasource

import kotlinx.coroutines.flow.Flow

/**
 * Domain интерфейс для локального кеширования Firebase токенов
 * Отвечает только за локальное хранение и получение токенов
 */
interface FirebaseTokenLocalDataSource {
    suspend fun saveToken(token: String)

    suspend fun getCachedToken(): String?

    fun observeTokenUpdates(): Flow<String>
}
