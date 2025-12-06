package com.shiplocate.domain.usecase.load

import com.shiplocate.domain.model.load.Route
import com.shiplocate.domain.repository.RouteRepository

/**
 * Use case to get cached route from local storage
 * Returns route for a specific load if it exists in cache
 */
class GetCachedRouteUseCase(
    private val routeRepository: RouteRepository,
) {
    /**
     * Get cached route for a load
     * @param loadId Load ID (server ID)
     * @return Route if found in cache, null otherwise
     */
    suspend operator fun invoke(loadId: Long): Route? {
        return routeRepository.getCachedRoute(loadId)
    }
}

