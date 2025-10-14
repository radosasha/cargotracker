package com.tracker.domain.repository

import com.tracker.domain.model.auth.AuthSession

/**
 * Repository for storing and retrieving authentication session
 */
interface AuthPreferencesRepository {
    suspend fun saveSession(session: AuthSession)
    suspend fun getSession(): AuthSession?
    suspend fun clearSession()
    suspend fun hasSession(): Boolean
}
