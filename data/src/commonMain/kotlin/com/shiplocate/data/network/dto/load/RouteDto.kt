package com.shiplocate.data.network.dto.load

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for Route from Google Routes API response
 * Matches server's GoogleRoutesResponse.Route structure
 */
@Serializable
data class RouteDto(
    @SerialName("distanceMeters")
    val distanceMeters: Long? = null,
    @SerialName("duration")
    val duration: String? = null,
    @SerialName("legs")
    val legs: List<LegDto>? = null,
)

@Serializable
data class LegDto(
    @SerialName("distanceMeters")
    val distanceMeters: Long? = null,
    @SerialName("duration")
    val duration: String? = null,
    @SerialName("polyline")
    val polyline: PolylineDto? = null,
    @SerialName("steps")
    val steps: List<StepDto>? = null,
)

@Serializable
data class StepDto(
    @SerialName("distanceMeters")
    val distanceMeters: Long? = null,
    @SerialName("staticDuration")
    val staticDuration: String? = null,
    @SerialName("polyline")
    val polyline: PolylineDto? = null,
    @SerialName("startLocation")
    val startLocation: WaypointLocationDto? = null,
    @SerialName("endLocation")
    val endLocation: WaypointLocationDto? = null,
    @SerialName("navigationInstruction")
    val navigationInstruction: NavigationInstructionDto? = null,
    @SerialName("localizedValues")
    val localizedValues: LocalizedValuesDto? = null,
    @SerialName("travelMode")
    val travelMode: String? = null,
)

@Serializable
data class WaypointLocationDto(
    @SerialName("latLng")
    val latLng: RouteLatLngDto? = null,
)

@Serializable
data class RouteLatLngDto(
    @SerialName("latitude")
    val latitude: Double? = null,
    @SerialName("longitude")
    val longitude: Double? = null,
)

@Serializable
data class PolylineDto(
    @SerialName("encodedPolyline")
    val encodedPolyline: String? = null,
)

@Serializable
data class NavigationInstructionDto(
    @SerialName("maneuver")
    val maneuver: String? = null,
    @SerialName("instructions")
    val instructions: String? = null,
)

@Serializable
data class LocalizedValuesDto(
    @SerialName("distance")
    val distance: LocalizedValueDto? = null,
    @SerialName("staticDuration")
    val staticDuration: LocalizedValueDto? = null,
)

@Serializable
data class LocalizedValueDto(
    @SerialName("text")
    val text: String? = null,
)

