package com.shiplocate.domain.usecase.auth

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.auth.AuthError
import com.shiplocate.domain.repository.AuthPreferencesRepository
import com.shiplocate.domain.repository.LoadRepository
import com.shiplocate.domain.repository.NotificationRepository
import com.shiplocate.domain.usecase.StopTrackingUseCase

/**
 * Use case for user logout
 * Clears all user data and session
 */
class LogoutUseCase(
    private val authPreferencesRepository: AuthPreferencesRepository,
    private val loadRepository: LoadRepository,
    private val notificationRepository: NotificationRepository,
    private val stopTrackingUseCase: StopTrackingUseCase,
    private val logger: Logger,
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            logger.info(LogCategory.AUTH, "LogoutUseCase: Starting logout process")

            // Step 1: Get current session token
            val session = authPreferencesRepository.getSession()
            val token = session?.token ?: return Result.success(Unit)

            // Step 2: Call logout API
            logger.info(LogCategory.AUTH, "LogoutUseCase: Calling logout API")
            val logoutResult = authPreferencesRepository.logout(token)
            if (logoutResult.isFailure) {
                val exception = logoutResult.exceptionOrNull()
                
                // Check if error is 401 Unauthorized - treat as success (token already invalidated)
                val isUnauthorized = when (exception) {
                    is AuthError.CodeInvalid -> {
                        // Fallback: CodeInvalid is created for 401 when response body can't be parsed
                        true
                    }
                    else -> false
                }
                
                if (isUnauthorized) {
                    logger.info(LogCategory.AUTH, "LogoutUseCase: Received 401 Unauthorized - token already invalidated, treating as success")
                    // Continue with logout process
                } else {
                    logger.error(LogCategory.AUTH, "LogoutUseCase: Logout API failed: ${exception?.message}")
                    return logoutResult
                }
            } else {
                logger.info(LogCategory.AUTH, "LogoutUseCase: Logout API succeeded")
            }

            // Step 3: Clear Firebase token from server
            try {
                notificationRepository.clearToken()
                logger.info(LogCategory.AUTH, "LogoutUseCase: Firebase token cleared")
            } catch (e: Exception) {
                logger.warn(LogCategory.AUTH, "LogoutUseCase: Failed to clear Firebase token: ${e.message}")
                // Continue with logout even if token clearing fails
            }

            val activeLoad = loadRepository.getConnectedLoad()
            if (activeLoad != null) {
                stopTrackingUseCase()
            }

            // Step 4: Clear all data from database
            logger.info(LogCategory.AUTH, "LogoutUseCase: Clearing all data from database")
            loadRepository.clearAllData()

            // Step 5: Clear auth session
            logger.info(LogCategory.AUTH, "LogoutUseCase: Clearing auth session")
            authPreferencesRepository.clearSession()

            logger.info(LogCategory.AUTH, "LogoutUseCase: ✅ Logout completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(LogCategory.AUTH, "LogoutUseCase: ❌ Logout failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}

