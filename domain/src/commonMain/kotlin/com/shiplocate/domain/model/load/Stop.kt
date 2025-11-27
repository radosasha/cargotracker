package com.shiplocate.domain.model.load

/**
 * Domain model for Stop (Drop)
 */
data class Stop(
    val id: Long,
    val type: Int,
    val locationName: String,
    val locationAddress: String,
    val date: Long,
    val geofenceRadius: Int,
    val index: Int,
    val latitude: Double,
    val longitude: Double,
    val enter: Boolean,
    val note: String? = null,
    val completion: Int = 0, // 0 = NOT_COMPLETED, 1 = COMPLETED
) {
    companion object {
        const val STOP_COMPLETION_NOT_COMPLETED = 0
        const val STOP_COMPLETION_COMPLETED = 1
    }
}

