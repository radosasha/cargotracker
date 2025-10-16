package com.tracker.domain.usecase.auth

import com.tracker.domain.model.auth.AuthSession
import com.tracker.domain.repository.AuthPreferencesRepository

/**
 * Use case for saving authentication session
 */
class SaveAuthSessionUseCase(
    private val authPreferencesRepository: AuthPreferencesRepository,
) {
    suspend operator fun invoke(session: AuthSession) {
        println("💾 SaveAuthSessionUseCase: Saving session for user: ${session.user.name}")
        authPreferencesRepository.saveSession(session)
        println("💾 SaveAuthSessionUseCase: ✅ Session saved")
    }
}
