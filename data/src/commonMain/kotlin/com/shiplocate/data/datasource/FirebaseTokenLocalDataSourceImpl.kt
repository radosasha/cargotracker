package com.shiplocate.data.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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
}
