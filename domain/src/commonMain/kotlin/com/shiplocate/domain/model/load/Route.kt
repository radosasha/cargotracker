package com.shiplocate.domain.model.load

/**
 * Domain model for Route from Google Routes API
 * Represents route information with legs and steps for navigation
 */
data class Route(
    val distanceMeters: Long? = null,
    val duration: String? = null,
    val legs: List<Leg>? = null,
)

data class Leg(
    val distanceMeters: Long? = null,
    val duration: String? = null,
    val polyline: Polyline? = null,
    val steps: List<Step>? = null,
)

data class Step(
    val distanceMeters: Long? = null,
    val staticDuration: String? = null,
    val polyline: Polyline? = null,
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

