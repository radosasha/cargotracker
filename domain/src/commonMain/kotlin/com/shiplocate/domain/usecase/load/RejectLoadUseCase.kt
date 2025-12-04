package com.shiplocate.domain.usecase.load

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
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
    private val logger: Logger,
) {
    /**
     * Reject load
     * Automatically retrieves auth token from preferences
     * @param loadId Internal ID of the load to reject
     * @return Result with updated list of loads
     */
    suspend operator fun invoke(loadId: Long): Result<List<Load>> {
        logger.info(LogCategory.GENERAL, "🚫 RejectLoadUseCase: Rejecting load with id: $loadId")

        // Get auth token
        val authSession = authRepository.getSession()
        val token = authSession?.token

        if (token == null) {
            logger.info(LogCategory.GENERAL, "❌ RejectLoadUseCase: Not authenticated")
            return Result.failure(Exception("Not authenticated"))
        }

        // Find the load by id to get its serverId
        val loads = loadRepository.getCachedLoads()
        val load = loads.find { it.id == loadId }

        if (load == null) {
            logger.info(LogCategory.GENERAL, "❌ RejectLoadUseCase: Load not found with id: $loadId")
            return Result.failure(Exception("Load not found"))
        }

        val rejectResult = loadRepository.rejectLoad(token, load.serverId)

        return rejectResult.fold({ it ->
            // Cache the updated results
            logger.info(LogCategory.GENERAL, "💾 RejectLoadUseCase: Saving ${it.size} loads to cache")
            loadRepository.saveLoads(it)

            // Return domain models
            val cachedLoads = loadRepository.getCachedLoads()
            logger.info(LogCategory.GENERAL, "✅ RejectLoadUseCase: Successfully rejected load ${load.serverId}")
            Result.success(cachedLoads)
        }, {
            Result.failure(it)
        })
    }
}

