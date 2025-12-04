package com.shiplocate.domain.usecase.auth

import com.shiplocate.domain.repository.AuthRepository

/**
 * Use case for checking if user has active session
 */
class HasAuthSessionUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Boolean {
        val hasSession = authRepository.hasSession()
        println("🔍 HasAuthSessionUseCase: Has session = $hasSession")
        return hasSession
    }
}
