package com.shiplocate.data.repository

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.data.datasource.auth.AuthPreferences
import com.shiplocate.data.datasource.remote.AuthRemoteDataSource
import com.shiplocate.domain.model.auth.AuthSession
import com.shiplocate.domain.model.auth.AuthToken
import com.shiplocate.domain.model.auth.AuthUser
import com.shiplocate.domain.model.auth.SmsCodeRequest
import com.shiplocate.domain.model.auth.SmsCodeResponse
import com.shiplocate.domain.model.auth.SmsCodeVerify
import com.shiplocate.domain.repository.AuthRepository

/**
 * Implementation of AuthPreferencesRepository using AuthPreferences
 */
class AuthRepositoryImpl(
    private val authPreferences: AuthPreferences,
    private val logger: Logger,
    private val authRemoteDataSource: AuthRemoteDataSource,
) : AuthRepository {


    override suspend fun requestSmsCode(request: SmsCodeRequest): Result<SmsCodeResponse> {
        return authRemoteDataSource.requestSmsCode(request)
    }

    override suspend fun verifySmsCode(verify: SmsCodeVerify): Result<AuthToken> {
        return authRemoteDataSource.verifySmsCode(verify)
    }

    override suspend fun saveSession(session: AuthSession) {
        logger.info(LogCategory.AUTH, "💾 AuthPreferencesRepository: Saving session for user: ${session.user.name} (${session.user.phone})")
        authPreferences.saveToken(session.token)
        authPreferences.saveUserId(session.user.id)
        authPreferences.saveUserPhone(session.user.phone)
        authPreferences.saveUserName(session.user.name)
        logger.info(LogCategory.AUTH, "💾 AuthPreferencesRepository: ✅ Session saved successfully")
    }

    override suspend fun getSession(): AuthSession? {
        logger.info(LogCategory.AUTH, "🔍 AuthPreferencesRepository: Getting session...")
        val token = authPreferences.getToken()
        val userId = authPreferences.getUserId()
        val userPhone = authPreferences.getUserPhone()
        val userName = authPreferences.getUserName()

        val session = if (token != null && userId != null && userPhone != null && userName != null) {
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

        if (session != null) {
            logger.info(LogCategory.AUTH, "🔍 AuthPreferencesRepository: ✅ Session found: ${session.user.name}")
        } else {
            logger.info(LogCategory.AUTH, "🔍 AuthPreferencesRepository: ⚠️ No session found")
        }

        return session
    }

    override suspend fun clearSession() {
        authPreferences.clearAll()
    }

    override suspend fun hasSession(): Boolean {
        val has = authPreferences.hasToken()
        logger.info(LogCategory.AUTH, "🔍 AuthPreferencesRepository: Has session = $has")
        return has
    }

    override suspend fun logout(token: String): Result<Unit> {
        logger.info(LogCategory.AUTH, "🌐 AuthPreferencesRepository: Logging out user")
        return try {
            val result = authRemoteDataSource.logout(token)
            if (result.isSuccess) {
                logger.info(LogCategory.AUTH, "🌐 AuthPreferencesRepository: ✅ Logout successful")
            } else {
                logger.error(LogCategory.AUTH, "🌐 AuthPreferencesRepository: ❌ Logout failed: ${result.exceptionOrNull()?.message}")
            }
            result
        } catch (e: Exception) {
            logger.error(LogCategory.AUTH, "🌐 AuthPreferencesRepository: ❌ Logout error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
