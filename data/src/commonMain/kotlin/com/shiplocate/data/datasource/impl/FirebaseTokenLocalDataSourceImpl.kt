package com.shiplocate.data.datasource.impl

import com.shiplocate.data.datasource.firebase.FirebasePreferences
import com.shiplocate.domain.datasource.FirebaseTokenLocalDataSource
import kotlinx.coroutines.flow.Flow

/**
 * Реализация FirebaseTokenLocalDataSource
 * Использует FirebasePreferences для локального кеширования токенов
 */
class FirebaseTokenLocalDataSourceImpl(
    private val firebasePreferences: FirebasePreferences,
) : FirebaseTokenLocalDataSource {

    override suspend fun saveToken(token: String) {
        firebasePreferences.saveToken(token)
    }

    override suspend fun getCachedToken(): String? {
        return firebasePreferences.getToken()
    }

    override fun observeTokenUpdates(): Flow<String> {
        return firebasePreferences.observeToken()
    }
    
    /**
     * Пометить токен как отправленный на сервер
     */
    override suspend fun markTokenAsSent() {
        firebasePreferences.saveTokenSent(true)
    }
    
    /**
     * Пометить токен как неотправленный на сервер
     */
    override suspend fun markTokenAsNotSent() {
        firebasePreferences.saveTokenSent(false)
    }
    
    /**
     * Проверить, был ли токен отправлен на сервер
     */
    override suspend fun isTokenSent(): Boolean {
        return firebasePreferences.isTokenSent()
    }
}
