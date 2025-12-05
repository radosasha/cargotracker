package com.shiplocate.domain.repository

import com.shiplocate.domain.model.load.Route

/**
 * Repository interface for Route data operations
 * Handles route caching and retrieval
 */
interface RouteRepository {
    /**
     * Get require update flag
     * @return true if route update is required, false otherwise
     */
    suspend fun getRequireUpdate(): Boolean

    /**
     * Save route for a load
     * @param loadId Load ID (server ID)
     * @param route Route domain model
     * @param provider Route provider (e.g., "google")
     * @param requireUpdate Whether route update is required
     */
    suspend fun saveRoute(
        loadId: Long,
        route: Route,
        provider: String,
        requireUpdate: Boolean = false,
    )

    /**
     * Set require update flag
     * @param requireUpdate Whether route update is required
     */
    suspend fun setRequireUpdate(requireUpdate: Boolean)

    /**
     * Get route for a load
     * @param loadId Load ID (server ID)
     * @return Route if found, null otherwise
     */
    suspend fun getRoute(loadId: Long): Route?

    /**
     * Delete cached route for a load
     * @param loadId Load ID (server ID)
     */
    suspend fun deleteRoute(loadId: Long)
}

