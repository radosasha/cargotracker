package com.shiplocate.domain.usecase.load

import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.repository.AuthPreferencesRepository
import com.shiplocate.domain.repository.LoadRepository

/**
 * Use case to get loads with automatic fallback to cache
 * First tries to fetch from server, falls back to cache if server is unavailable
 */
class GetLoadsUseCase(
    private val loadRepository: LoadRepository,
    private val authPreferencesRepository: AuthPreferencesRepository,
) {
    /**
     * Get loads from server or cache
     * Automatically retrieves auth token from preferences
     * @return Result with list of loads
     */
    suspend operator fun invoke(): Result<List<Load>> {
        // Get auth token
        val authSession = authPreferencesRepository.getSession()
        val token = authSession?.token

        if (token == null) {
            return Result.failure(Exception("Not authenticated"))
        }

        return loadRepository.getLoads(token)
    }
}
