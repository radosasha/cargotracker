package com.tracker.domain.usecase.auth

import com.tracker.domain.model.auth.AuthSession
import com.tracker.domain.repository.AuthPreferencesRepository

/**
 * Use case for getting current authentication session
 */
class GetAuthSessionUseCase(
    private val authPreferencesRepository: AuthPreferencesRepository
) {
    suspend operator fun invoke(): AuthSession? {
        return authPreferencesRepository.getSession()
    }
}
