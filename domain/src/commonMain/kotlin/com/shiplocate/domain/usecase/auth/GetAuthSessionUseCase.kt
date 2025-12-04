package com.shiplocate.domain.usecase.auth

import com.shiplocate.domain.model.auth.AuthSession
import com.shiplocate.domain.repository.AuthRepository

/**
 * Use case for getting current authentication session
 */
class GetAuthSessionUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): AuthSession? {
        return authRepository.getSession()
    }
}
