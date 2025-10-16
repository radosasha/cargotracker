package com.tracker.domain.usecase.load

import com.tracker.domain.model.load.Load
import com.tracker.domain.repository.AuthPreferencesRepository
import com.tracker.domain.repository.LoadRepository

/**
 * Use case to connect to a load
 * Sets loadstatus=1 for the specified load and loadstatus=2 for all other loads with loadstatus=1
 */
class ConnectToLoadUseCase(
    private val loadRepository: LoadRepository,
    private val authPreferencesRepository: AuthPreferencesRepository
) {
    /**
     * Connect to load
     * Automatically retrieves auth token from preferences
     * @param loadId Load ID to connect to
     * @return Result with updated list of loads
     */
    suspend operator fun invoke(loadId: String): Result<List<Load>> {
        println("🔌 ConnectToLoadUseCase: Connecting to load $loadId")
        
        // Get auth token
        val authSession = authPreferencesRepository.getSession()
        val token = authSession?.token
        
        if (token == null) {
            println("❌ ConnectToLoadUseCase: Not authenticated")
            return Result.failure(Exception("Not authenticated"))
        }
        
        return loadRepository.connectToLoad(token, loadId)
    }
}
