package com.shiplocate.trackingsdk.parking.models

sealed class ParkingStatus {
    data class InParking(val reason: InReason) : ParkingStatus()
}

enum class InReason {
    Radius,   // detected by long presence within radius
    Timeout,  // keep-alive when GPS is silent
}


