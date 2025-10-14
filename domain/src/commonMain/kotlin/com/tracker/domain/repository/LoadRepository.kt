package com.tracker.domain.repository

import com.tracker.domain.model.load.Load

/**
 * Repository interface for Load data operations
 * Handles both remote API calls and local caching
 */
interface LoadRepository {
    /**
     * Get loads from remote server with fallback to cache
     * @param token Authentication token
     * @return List of loads, either from server or cache
     */
    suspend fun getLoads(token: String): Result<List<Load>>
    
    /**
     * Get cached loads from local database
     * @return List of cached loads
     */
    suspend fun getCachedLoads(): List<Load>
    
    /**
     * Clear all cached loads
     */
    suspend fun clearCache()
}

