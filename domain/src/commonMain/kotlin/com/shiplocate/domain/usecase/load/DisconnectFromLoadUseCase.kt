package com.shiplocate.domain.usecase.load

import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.repository.AuthPreferencesRepository
import com.shiplocate.domain.repository.LoadRepository

/**
 * Use case to disconnect from a load
 * Sets loadstatus=2 for the specified load
 */
class DisconnectFromLoadUseCase(
    private val loadRepository: LoadRepository,
    private val authPreferencesRepository: AuthPreferencesRepository,
) {
    /**
     * Disconnect from load
     * Automatically retrieves auth token from preferences
     * @param loadId Load ID to disconnect from
     * @return Result with updated list of loads
     */
    suspend operator fun invoke(loadId: String): Result<List<Load>> {
        println("🔌 DisconnectFromLoadUseCase: Disconnecting from load $loadId")

        // Get auth token
        val authSession = authPreferencesRepository.getSession()
        val token = authSession?.token

        if (token == null) {
            println("❌ DisconnectFromLoadUseCase: Not authenticated")
            return Result.failure(Exception("Not authenticated"))
        }

        return loadRepository.disconnectFromLoad(token, loadId)
    }
}
