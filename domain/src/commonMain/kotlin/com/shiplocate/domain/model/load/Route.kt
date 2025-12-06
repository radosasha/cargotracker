package com.shiplocate.domain.model.load

/**
 * Domain model for Route from Google Routes API
 * Represents route information with legs and steps for navigation
 *
 * Note: All fields are nullable because Google Routes API does not guarantee
 * the presence of fields in the response, even if they are requested in fieldMask.
 * Fields may be absent if the API cannot calculate them or in error cases.
 */
data class Route(
    val distanceMeters: Long? = null,
    val duration: Int? = null,
    val legs: List<Leg>? = null,
)

data class Leg(
    val distanceMeters: Long? = null,
    val duration: Int? = null,
    val polyline: Polyline? = null,
    val steps: List<Step>? = null,
)

data class Step(
    val distanceMeters: Long? = null,
    val staticDuration: Int? = null,
    val polyline: List<StepCoordinate>? = null,
    val startLocation: WaypointLocation? = null,
    val endLocation: WaypointLocation? = null,
    val navigationInstruction: NavigationInstruction? = null,
    val localizedValues: LocalizedValues? = null,
    val travelMode: String? = null,
)

data class WaypointLocation(
    val latLng: RouteLatLng? = null,
)

data class RouteLatLng(
    val latitude: Double? = null,
    val longitude: Double? = null,
)

data class Polyline(
    val encodedPolyline: String? = null,
)

data class NavigationInstruction(
    val maneuver: String? = null,
    val instructions: String? = null,
)

data class LocalizedValues(
    val distance: LocalizedValue? = null,
    val staticDuration: LocalizedValue? = null,
)

data class LocalizedValue(
    val text: String? = null,
)

