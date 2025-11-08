package com.shiplocate.domain.model

import kotlinx.datetime.Instant

class DeviceLocation (
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double? = null,
    val speed: Float? = null,
    val bearing: Float? = null,
    val timestamp: Instant,
    val batteryLevel: Float? = null
)
