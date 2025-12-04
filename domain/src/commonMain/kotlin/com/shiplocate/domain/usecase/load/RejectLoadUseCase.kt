package com.shiplocate.domain.usecase.load

import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.repository.AuthRepository
import com.shiplocate.domain.repository.LoadRepository

/**
 * Use case to reject a load
 * Rejects the specified load
 */
class RejectLoadUseCase(
    private val loadRepository: LoadRepository,
    private val authRepository: AuthRepository,
) {
    /**
     * Reject load
     * Automatically retrieves auth token from preferences
     * @param loadId Internal ID of the load to reject
     * @return Result with updated list of loads
     */
    suspend operator fun invoke(loadId: Long): Result<List<Load>> {
        println("🚫 RejectLoadUseCase: Rejecting load with id: $loadId")

        // Get auth token
        val authSession = authRepository.getSession()
        val token = authSession?.token

        if (token == null) {
            println("❌ RejectLoadUseCase: Not authenticated")
            return Result.failure(Exception("Not authenticated"))
        }

        // Find the load by id to get its serverId
        val loads = loadRepository.getCachedLoads()
        val load = loads.find { it.id == loadId }

        if (load == null) {
            println("❌ RejectLoadUseCase: Load not found with id: $loadId")
            return Result.failure(Exception("Load not found"))
        }

        return loadRepository.rejectLoad(token, load.serverId)
    }
}

