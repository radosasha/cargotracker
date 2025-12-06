package com.shiplocate.domain.usecase.load

import com.shiplocate.domain.model.load.Route
import com.shiplocate.domain.repository.RouteRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe cached route changes from local storage
 * Subscribes to route changes via RouteRepository
 */
class ObserveCachedRouteUseCase(
    private val routeRepository: RouteRepository,
) {
    /**
     * Observe route changes from data store
     * @return Flow of Route, or null if not found
     */
    operator fun invoke(): Flow<Route?> {
        return routeRepository.observeRoute()
    }
}

