package com.copperleaf.ballast.navigation.routing

import com.copperleaf.ballast.navigation.internal.RouteParser

public interface RouteMatcher {

    public val routeFormat: String
    public val path: List<PathSegment>
    public val query: List<QueryParameter>
    public val weight: Double

    public fun <T : Route> match(
        originalRoute: T,
        unmatchedDestination: UnmatchedDestination,
    ): MatchResult<T>

    public sealed interface MatchResult<T : Route> {
        public val originalRoute: T

        /**
         * Neither the path not the query parameters matched this route.
         */
        public data class NoMatch<T : Route>(
            override val originalRoute: T,
        ) : MatchResult<T>

        /**
         * A partial match, where the path was fully matched against the input destination URL, but the query parameters
         * do not match (either missing required query parameters, or extras were provided in the URL that could not be
         * matched to this route).
         */
        public data class PartialMatch<T : Route>(
            override val originalRoute: T,
            val parsedPathParameters: Map<String, List<String>>,
        ) : MatchResult<T>

        /**
         * A complete match, both the path and query are fully matched against the input destination URL
         */
        public data class CompleteMatch<T : Route>(
            override val originalRoute: T,
            val parsedPathParameters: Map<String, List<String>>,
            val parsedQueryParameters: Map<String, List<String>>,
        ) : MatchResult<T>
    }

    public companion object {
        public fun create(
            routeFormat: String,
            computeWeight: (List<PathSegment>, List<QueryParameter>) -> Double = RouteParser::computeWeight,
        ): RouteMatcher = RouteParser.parseRoute(routeFormat, computeWeight)
    }
}
