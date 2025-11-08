package com.shiplocate.domain.usecase.load

import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.repository.AuthPreferencesRepository
import com.shiplocate.domain.repository.LoadRepository

/**
 * Use case to connect to a load
 * Sets loadstatus=1 for the specified load and loadstatus=2 for all other loads with loadstatus=1
 */
class ConnectToLoadUseCase(
    private val loadRepository: LoadRepository,
    private val authPreferencesRepository: AuthPreferencesRepository,
) {
    /**
     * Connect to load
     * Automatically retrieves auth token from preferences
     * @param loadId Internal ID of the load to connect to
     * @return Result with updated list of loads
     */
    suspend operator fun invoke(loadId: Long): Result<List<Load>> {
        println("🔌 ConnectToLoadUseCase: Connecting to load with id: $loadId")

        // Get auth token
        val authSession = authPreferencesRepository.getSession()
        val token = authSession?.token

        if (token == null) {
            println("❌ ConnectToLoadUseCase: Not authenticated")
            return Result.failure(Exception("Not authenticated"))
        }

        // Find the load by id to get its serverId
        val loads = loadRepository.getCachedLoads()
        val load = loads.find { it.id == loadId }

        if (load == null) {
            println("❌ ConnectToLoadUseCase: Load not found with id: $loadId")
            return Result.failure(Exception("Load not found"))
        }

        return loadRepository.connectToLoad(token, load.serverId)
    }
}
