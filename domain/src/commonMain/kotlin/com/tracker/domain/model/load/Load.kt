package com.tracker.domain.model.load

/**
 * Domain model for Load (Device)
 * Represents a load/device that belongs to the authenticated user
 */
data class Load(
    val loadId: String,
    val description: String?,
    val lastUpdated: Long?,
    val createdAt: Long,
    val loadStatus: Int
)



