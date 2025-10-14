package com.tracker.domain.usecase.auth

import com.tracker.domain.repository.AuthPreferencesRepository

/**
 * Use case for clearing authentication session (logout)
 */
class ClearAuthSessionUseCase(
    private val authPreferencesRepository: AuthPreferencesRepository
) {
    suspend operator fun invoke() {
        authPreferencesRepository.clearSession()
    }
}




