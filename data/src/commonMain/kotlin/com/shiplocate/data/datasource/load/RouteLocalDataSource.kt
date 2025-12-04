package com.shiplocate.data.datasource.load

/**
 * Local data source for Route caching
 * Handles local storage operations for routes
 */
interface RouteLocalDataSource {
    /**
     * Save route for a load
     * @param loadId Load ID
     * @param routeJson Route as JSON string
     * @param provider Route provider (e.g., "google")
     */
    suspend fun saveRoute(
        loadId: Long,
        routeJson: String,
        provider: String,
    )

    /**
     * Get route for a load
     * @param loadId Load ID
     * @return Route as JSON string, or null if not found
     */
    suspend fun getRoute(loadId: Long): String?

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
    suspend fun deleteRoute(loadId: Long)

    /**
     * Clear all routes
     */
    suspend fun clearAllRoutes()
}

