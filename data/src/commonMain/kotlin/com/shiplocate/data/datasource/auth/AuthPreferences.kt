package com.shiplocate.data.datasource.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Encapsulates DataStore operations for Auth preferences
 */
class AuthPreferences(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        private val KEY_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USER_ID = longPreferencesKey("auth_user_id")
        private val KEY_USER_PHONE = stringPreferencesKey("auth_user_phone")
        private val KEY_USER_NAME = stringPreferencesKey("auth_user_name")
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token
        }
    }

    suspend fun getToken(): String? {
        return dataStore.data.first()[KEY_TOKEN]
    }

    suspend fun saveUserId(userId: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = userId
        }
    }

    suspend fun getUserId(): Long? {
        return dataStore.data.first()[KEY_USER_ID]
    }

    suspend fun saveUserPhone(phone: String) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_PHONE] = phone
        }
    }

    suspend fun getUserPhone(): String? {
        return dataStore.data.first()[KEY_USER_PHONE]
    }

    suspend fun saveUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_NAME] = name
        }
    }

    suspend fun getUserName(): String? {
        return dataStore.data.first()[KEY_USER_NAME]
    }

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_TOKEN)
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_USER_PHONE)
            preferences.remove(KEY_USER_NAME)
        }
    }

    suspend fun hasToken(): Boolean {
        return dataStore.data.map { preferences ->
            preferences[KEY_TOKEN] != null
        }.first()
    }
}

