package com.copperleaf.ballast.navigation.routing

import io.ktor.http.Url

/**
 * Represents a URL that was parsed, and can be used to match against a [Route] in a [RoutingTable].
 */
public data class UnmatchedDestination(
    val originalDestinationUrl: String,

    val matchablePathSegments: List<String>,
    val matchableQueryParameters: Map<String, List<String>>,

    public val extraAnnotations: Set<RouteAnnotation>,
) {
    public companion object {
        public fun parse(
            destinationUrl: String,
            extraAnnotations: Set<RouteAnnotation> = emptySet(),
        ): UnmatchedDestination {
            val url = Url(destinationUrl)

            val matchablePathSegments = url.pathSegments.dropWhile { it.isBlank() }.dropLastWhile { it.isBlank() }
            val matchableQueryParameters = url.parameters.entries().associate { it.key to it.value }

            return UnmatchedDestination(
                originalDestinationUrl = destinationUrl,
                matchablePathSegments = matchablePathSegments,
                matchableQueryParameters = matchableQueryParameters,
                extraAnnotations = extraAnnotations,
            )
        }
    }
}
