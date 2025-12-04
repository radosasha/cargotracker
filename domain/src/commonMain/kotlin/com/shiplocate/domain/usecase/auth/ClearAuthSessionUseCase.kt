package com.shiplocate.domain.usecase.auth

import com.shiplocate.domain.repository.AuthRepository

/**
 * Use case for clearing authentication session (logout)
 */
class ClearAuthSessionUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() {
        authRepository.clearSession()
    }
}
