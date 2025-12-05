package com.shiplocate.domain.usecase.load

import com.shiplocate.core.logging.LogCategory
import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.repository.AuthRepository
import com.shiplocate.domain.repository.LoadRepository
import com.shiplocate.domain.repository.RouteRepository

/**
 * Use case to connect to a load
 * Sets loadstatus=1 for the specified load and loadstatus=2 for all other loads with loadstatus=1
 */
class ConnectToLoadUseCase(
    private val loadRepository: LoadRepository,
    private val authRepository: AuthRepository,
    private val routeRepository: RouteRepository,
    private val logger: Logger,
) {
    /**
     * Connect to load
     * Automatically retrieves auth token from preferences
     * @param loadId Internal ID of the load to connect to
     * @return Result with updated list of loads
     */
    suspend operator fun invoke(loadId: Long): Result<List<Load>> {
        logger.info(LogCategory.GENERAL, "🔌 ConnectToLoadUseCase: Connecting to load with id: $loadId")

        // Get auth token
        val authSession = authRepository.getSession()
        val token = authSession?.token

        if (token == null) {
            logger.info(LogCategory.GENERAL, "❌ ConnectToLoadUseCase: Not authenticated")
            return Result.failure(Exception("Not authenticated"))
        }

        // Find the load by id to get its serverId
        val loads = loadRepository.getCachedLoads()
        val load = loads.find { it.id == loadId }

        if (load == null) {
            logger.info(LogCategory.GENERAL, "❌ ConnectToLoadUseCase: Load not found with id: $loadId")
            return Result.failure(Exception("Load not found"))
        }

        val connectedResult = loadRepository.connectToLoad(token, load.serverId)
        return connectedResult.fold(
            {
                // Cache the updated results
                logger.info(LogCategory.GENERAL, "💾 ConnectToLoadUseCase: Saving ${it.size} loads to cache")

                loadRepository.saveLoads(it)

                // Return domain models
                val loads = loadRepository.getCachedLoads()
                logger.info(LogCategory.GENERAL, "✅ ConnectToLoadUseCase: Successfully connected to load ${load.serverId}")
                routeRepository.setRequireUpdate(load.stops.size >= 2)
                Result.success(loads)
            },
            {
                Result.failure(it)
            }
        )
    }
}
