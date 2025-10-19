package com.shiplocate.data.datasource.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.shiplocate.domain.datasource.FirebaseTokenLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Реализация FirebaseTokenLocalDataSource
 * Использует DataStore для локального кеширования токенов
 */
class FirebaseTokenLocalDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
) : FirebaseTokenLocalDataSource {
    companion object {
        private val FIREBASE_TOKEN_KEY = stringPreferencesKey("firebase_token")
        private val FIREBASE_TOKEN_SENT_KEY = booleanPreferencesKey("firebase_token_sent")
    }

    override suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[FIREBASE_TOKEN_KEY] = token
        }
    }

    override suspend fun getCachedToken(): String? {
        return dataStore.data.map { preferences ->
            preferences[FIREBASE_TOKEN_KEY]
        }.first()
    }

    override fun observeTokenUpdates(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[FIREBASE_TOKEN_KEY] ?: ""
        }
    }
    
    /**
     * Пометить токен как отправленный на сервер
     */
    override suspend fun markTokenAsSent() {
        dataStore.edit { preferences ->
            markTokenAsSent(preferences)
        }
        println("FirebaseTokenLocalDataSource: Token marked as sent to server")
    }
    
    /**
     * Пометить токен как неотправленный на сервер
     */
    override suspend fun markTokenAsNotSent() {
        dataStore.edit { preferences ->
            markTokenAsNotSent(preferences)
        }
        println("FirebaseTokenLocalDataSource: Token marked as not sent")
    }
    
    /**
     * Проверить, был ли токен отправлен на сервер
     */
    override suspend fun isTokenSent(): Boolean {
        return dataStore.data.map { preferences ->
            isTokenSent(preferences)
        }.first()
    }

    /**
     * Пометить токен как отправленный
     */
    private fun markTokenAsSent(preferences: MutablePreferences) {
        preferences[FIREBASE_TOKEN_SENT_KEY] = true
    }
    
    /**
     * Пометить токен как неотправленный
     */
    private fun markTokenAsNotSent(preferences: MutablePreferences) {
        preferences[FIREBASE_TOKEN_SENT_KEY] = false
    }
    
    /**
     * Получить статус отправки токена
     */
    private fun isTokenSent(preferences: Preferences): Boolean {
        return preferences[FIREBASE_TOKEN_SENT_KEY] ?: false
    }
}
