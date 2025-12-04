package com.shiplocate.domain.usecase.auth

import com.shiplocate.domain.model.auth.AuthSession
import com.shiplocate.domain.repository.AuthRepository

/**
 * Use case for saving authentication session
 */
class SaveAuthSessionUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(session: AuthSession) {
        println("💾 SaveAuthSessionUseCase: Saving session for user: ${session.user.name}")
        authRepository.saveSession(session)
        println("💾 SaveAuthSessionUseCase: ✅ Session saved")
    }
}
