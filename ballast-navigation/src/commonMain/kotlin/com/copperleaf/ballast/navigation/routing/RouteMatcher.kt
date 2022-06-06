package com.copperleaf.ballast.navigation.routing

public data class RouteMatcher(
    val path: List<PathSegment>,
    val weight: Double,
)
