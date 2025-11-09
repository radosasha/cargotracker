package com.shiplocate.domain.model.load

/**
 * Domain model for Stop (Drop)
 */
data class Stop(
    val id: Long,
    val type: Int,
    val locationAddress: String,
    val date: Long,
    val geofenceRadius: Int,
    val index: Int,
)

