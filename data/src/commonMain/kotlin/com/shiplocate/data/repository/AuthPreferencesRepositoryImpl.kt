package com.shiplocate.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.auth.AuthSession
import com.shiplocate.domain.model.auth.AuthUser
import com.shiplocate.domain.repository.AuthPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Implementation of AuthPreferencesRepository using DataStore
 */
class AuthPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val logger: Logger,
) : AuthPreferencesRepository {
    companion object {
        private val KEY_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USER_ID = longPreferencesKey("auth_user_id")
        private val KEY_USER_PHONE = stringPreferencesKey("auth_user_phone")
        private val KEY_USER_NAME = stringPreferencesKey("auth_user_name")
    }

    override suspend fun saveSession(session: AuthSession) {
        logger.info(LogCategory.AUTH, "💾 AuthPreferencesRepository: Saving session for user: ${session.user.name} (${session.user.phone})")
        dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = session.token
            preferences[KEY_USER_ID] = session.user.id
            preferences[KEY_USER_PHONE] = session.user.phone
            preferences[KEY_USER_NAME] = session.user.name
        }
        logger.info(LogCategory.AUTH, "💾 AuthPreferencesRepository: ✅ Session saved successfully")
    }

    override suspend fun getSession(): AuthSession? {
        logger.info(LogCategory.AUTH, "🔍 AuthPreferencesRepository: Getting session...")
        val session =
            dataStore.data.map { preferences ->
                val token = preferences[KEY_TOKEN]
                val userId = preferences[KEY_USER_ID]
                val userPhone = preferences[KEY_USER_PHONE]
                val userName = preferences[KEY_USER_NAME]

                if (token != null && userId != null && userPhone != null && userName != null) {
                    AuthSession(
                        token = token,
                        user =
                            AuthUser(
                                id = userId,
                                phone = userPhone,
                                name = userName,
                            ),
                    )
                } else {
                    null
                }
            }.first()

        if (session != null) {
            logger.info(LogCategory.AUTH, "🔍 AuthPreferencesRepository: ✅ Session found: ${session.user.name}")
        } else {
            logger.info(LogCategory.AUTH, "🔍 AuthPreferencesRepository: ⚠️ No session found")
        }

        return session
    }

    override suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_TOKEN)
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_USER_PHONE)
            preferences.remove(KEY_USER_NAME)
        }
    }

    override suspend fun hasSession(): Boolean {
        val has =
            dataStore.data.map { preferences ->
                preferences[KEY_TOKEN] != null
            }.first()
        logger.info(LogCategory.AUTH, "🔍 AuthPreferencesRepository: Has session = $has")
        return has
    }
}
