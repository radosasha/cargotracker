package com.shiplocate.data.datasource.firebase

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Encapsulates DataStore operations for Firebase preferences
 */
class FirebasePreferences(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        private val KEY_TOKEN = stringPreferencesKey("firebase_token")
        private val KEY_TOKEN_SENT = booleanPreferencesKey("firebase_token_sent")
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token
        }
    }

    suspend fun getToken(): String? {
        return dataStore.data.first()[KEY_TOKEN]
    }

    suspend fun saveTokenSent(sent: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_TOKEN_SENT] = sent
        }
    }

    suspend fun isTokenSent(): Boolean {
        return dataStore.data.map { preferences ->
            preferences[KEY_TOKEN_SENT] ?: false
        }.first()
    }

    fun observeToken(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[KEY_TOKEN] ?: ""
        }
    }

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_TOKEN)
            preferences.remove(KEY_TOKEN_SENT)
        }
    }
}

