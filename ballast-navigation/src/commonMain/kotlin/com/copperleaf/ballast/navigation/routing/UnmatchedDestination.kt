package com.copperleaf.ballast.navigation.routing

public data class UnmatchedDestination(
    val originalUrl: String,

    val path: String,
    val pathSegments: List<String>,
    val queryParameters: Map<String, List<String>>,
)
