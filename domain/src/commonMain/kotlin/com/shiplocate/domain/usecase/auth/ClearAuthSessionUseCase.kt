package com.shiplocate.domain.usecase.auth

import com.shiplocate.domain.repository.AuthPreferencesRepository

/**
 * Use case for clearing authentication session (logout)
 */
class ClearAuthSessionUseCase(
    private val authPreferencesRepository: AuthPreferencesRepository,
) {
    suspend operator fun invoke() {
        authPreferencesRepository.clearSession()
    }
}
