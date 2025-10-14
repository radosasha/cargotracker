package com.tracker.domain.usecase.auth

import com.tracker.domain.repository.AuthPreferencesRepository

/**
 * Use case for checking if user has active session
 */
class HasAuthSessionUseCase(
    private val authPreferencesRepository: AuthPreferencesRepository
) {
    suspend operator fun invoke(): Boolean {
        val hasSession = authPreferencesRepository.hasSession()
        println("🔍 HasAuthSessionUseCase: Has session = $hasSession")
        return hasSession
    }
}

