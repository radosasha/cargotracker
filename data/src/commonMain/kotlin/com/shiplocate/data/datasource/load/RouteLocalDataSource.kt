package com.shiplocate.data.datasource.load

import com.shiplocate.domain.model.load.Route

/**
 * Local data source for Route caching
 * Handles local storage operations for routes
 */
interface RouteLocalDataSource {
    /**
     * Save route for a load
     * @param loadId Load ID
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
     * Get route for a load
     * @param loadId Load ID
     * @return Route domain model, or null if not found
     */
    suspend fun getRoute(loadId: Long): Route?

    /**
     * Get route provider for a load
     * @param loadId Load ID
     * @return Route provider, or null if not found
     */
    suspend fun getRouteProvider(loadId: Long): String?

    /**
     * Delete route for a load
     * @param loadId Load ID
     */
    suspend fun deleteRoute(loadId: Long): Boolean

    /**
     * Clear all routes
     */
    suspend fun clearAllRoutes()

    /**
     * Get require update flag
     * @return true if route update is required, false otherwise
     */
    suspend fun getRequireUpdate(): Boolean

    /**
     * Set require update flag
     * @param requireUpdate Whether route update is required
     */
    suspend fun setRequireUpdate(requireUpdate: Boolean)

    /**
     * Get saved load ID
     * @return Load ID if found, null otherwise
     */
    suspend fun getLoadId(): Long?
}

