package com.copperleaf.ballast.navigation.routing

public sealed interface NavToken

public data class Tag(
    val tag: String,
) : NavToken

public data class Destination(
    public val originalUrl: String,
    public val originalRoute: Route,

    public val path: String,
    public val pathSegments: List<String>,
    public val pathParameters: Map<String, List<String>>,
    public val queryParameters: Map<String, List<String>>,
) : NavToken

public data class MissingDestination(
    public val originalUrl: String,
) : NavToken
