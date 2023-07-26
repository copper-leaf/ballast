package com.copperleaf.ballast.navigation.routing

import com.copperleaf.ballast.navigation.internal.Uri

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
            val url = Uri.parse(destinationUrl)

            return UnmatchedDestination(
                originalDestinationUrl = destinationUrl,
                matchablePathSegments = url.decodedPathSegments,
                matchableQueryParameters = url.decodedQueryParameters,
                extraAnnotations = extraAnnotations,
            )
        }
    }
}
