package com.shiplocate.trackingsdk.parking.models

data class ParkingLocation(
    val lat: Double,
    val lon: Double,
    val time: Long,
    val error: Int
)
