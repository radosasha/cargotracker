package com.shiplocate.domain.usecase.load

import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.repository.AuthRepository
import com.shiplocate.domain.repository.LoadRepository

/**
 * Use case to disconnect from a load
 * Sets loadstatus=2 for the specified load
 */
class DisconnectFromLoadUseCase(
    private val loadRepository: LoadRepository,
    private val authRepository: AuthRepository,
) {
    /**
     * Disconnect from load
     * Automatically retrieves auth token from preferences
     * @param loadId Internal ID of the load to disconnect from
     * @return Result with updated list of loads
     */
    suspend operator fun invoke(loadId: Long): Result<List<Load>> {
        println("🔌 DisconnectFromLoadUseCase: Disconnecting from load with id: $loadId")

        // Get auth token
        val authSession = authRepository.getSession()
        val token = authSession?.token

        if (token == null) {
            println("❌ DisconnectFromLoadUseCase: Not authenticated")
            return Result.failure(Exception("Not authenticated"))
        }

        // Find the load by id to get its serverId
        val loads = loadRepository.getCachedLoads()
        val load = loads.find { it.id == loadId }

        if (load == null) {
            println("❌ DisconnectFromLoadUseCase: Load not found with id: $loadId")
            return Result.failure(Exception("Load not found"))
        }

        return loadRepository.disconnectFromLoad(token, load.serverId)
    }
}
