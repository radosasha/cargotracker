package com.shiplocate.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for coordinates request to mobile API
 * Matches server's CoordinatesRequest structure
 */
@Serializable
data class CoordinatesRequestDto(
    @SerialName("loadId")
    val loadId: Long,
    @SerialName("coordinates")
    val coordinates: List<CoordinateDto>,
)

/**
 * DTO for a single coordinate
 * Matches server's CoordinatesRequest.CoordinateData structure
 * Note: Server expects lat/lon (not latitude/longitude), bearing (not course), batt (not batteryLevel)
 * Speed should be in knots (not m/s)
 */
@Serializable
data class CoordinateDto(
    @SerialName("lat")
    val lat: Double,
    @SerialName("lon")
    val lon: Double,
    @SerialName("timestamp")
    val timestamp: Long, // Unix timestamp in milliseconds (or seconds if < Integer.MAX_VALUE)
    @SerialName("valid")
    val valid: Boolean = true,
    @SerialName("speed")
    val speed: Double? = null, // in knots (not m/s!)
    @SerialName("bearing")
    val bearing: Double? = null, // in degrees (preferred over heading)
    @SerialName("heading")
    val heading: Double? = null, // in degrees (fallback if bearing not available)
    @SerialName("altitude")
    val altitude: Double? = null, // in meters
    @SerialName("accuracy")
    val accuracy: Double? = null, // in meters
    @SerialName("batt")
    val batt: Double? = null, // battery level as Double (0-100)
    @SerialName("charge")
    val charge: Boolean? = null, // charging status
    @SerialName("hdop")
    val hdop: Double? = null, // horizontal dilution of precision
    @SerialName("driverUniqueId")
    val driverUniqueId: String? = null, // driver unique ID
)

