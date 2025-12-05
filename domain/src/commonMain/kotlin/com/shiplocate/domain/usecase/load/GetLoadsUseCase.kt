package com.shiplocate.domain.usecase.load

import com.shiplocate.core.logging.Logger
import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.repository.AuthRepository
import com.shiplocate.domain.repository.LoadRepository
import com.shiplocate.domain.repository.RouteRepository

/**
 * Use case to get loads with automatic fallback to cache
 * First tries to fetch from server, falls back to cache if server is unavailable
 */
class GetLoadsUseCase(
    loadRepository: LoadRepository,
    authRepository: AuthRepository,
    logger: Logger,
    routeRepository: RouteRepository,
) : BaseLoadsUseCase(loadRepository, authRepository, logger, routeRepository) {
    /**
     * Get loads from server or cache
     * Automatically retrieves auth token from preferences
     * @return Result with list of loads
     */
    suspend operator fun invoke(): Result<List<Load>> {
        return getLoads()
    }
}
