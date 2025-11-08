package com.shiplocate.domain.model.load

/**
 * Domain model for Load (Device)
 * Represents a load/device that belongs to the authenticated user
 *
 * Field usage:
 * - id: Internal ID used within the application for operations
 * - serverId: ID used for API calls to the server (must match server's ID)
 * - loadName: Name/uniqueId used for UI display and navigation
 */
data class Load(
    val id: Long, // Internal ID for application operations
    val serverId: Long, // ID for API calls to server
    val loadName: String, // Name for UI display
    val description: String?,
    val lastUpdated: Long?,
    val createdAt: Long,
    val loadStatus: Int,
)
