package com.shiplocate.domain.usecase.auth

import com.shiplocate.domain.model.auth.AuthSession
import com.shiplocate.domain.repository.AuthPreferencesRepository

/**
 * Use case for getting current authentication session
 */
class GetAuthSessionUseCase(
    private val authPreferencesRepository: AuthPreferencesRepository,
) {
    suspend operator fun invoke(): AuthSession? {
        return authPreferencesRepository.getSession()
    }
}
