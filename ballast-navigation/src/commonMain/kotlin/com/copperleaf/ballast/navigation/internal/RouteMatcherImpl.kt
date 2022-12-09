package com.copperleaf.ballast.navigation.internal

import com.copperleaf.ballast.navigation.routing.PathSegment
import com.copperleaf.ballast.navigation.routing.QueryParameter
import com.copperleaf.ballast.navigation.routing.Route
import com.copperleaf.ballast.navigation.routing.RouteMatcher
import com.copperleaf.ballast.navigation.routing.UnmatchedDestination

internal data class RouteMatcherImpl(
    override val routeFormat: String,
    override val path: List<PathSegment>,
    override val query: List<QueryParameter>,
    override val weight: Double,
) : RouteMatcher {
// Main matcher
// ---------------------------------------------------------------------------------------------------------------------

    override fun <T : Route> match(
        originalRoute: T,
        unmatchedDestination: UnmatchedDestination,
    ): RouteMatcher.MatchResult<T> {
        return when (val pathMatchResult = matchPath(unmatchedDestination)) {
            is PathMatchResult.Mismatch -> RouteMatcher.MatchResult.NoMatch(originalRoute)
            is PathMatchResult.Match -> {
                when (val queryMatchResult = matchQuery(unmatchedDestination)) {
                    is QueryMatchResult.Mismatch -> RouteMatcher.MatchResult.PartialMatch(
                        originalRoute = originalRoute,
                        parsedPathParameters = pathMatchResult.parsedParameters,
                    )
                    is QueryMatchResult.Match -> RouteMatcher.MatchResult.CompleteMatch(
                        originalRoute = originalRoute,
                        parsedPathParameters = pathMatchResult.parsedParameters,
                        parsedQueryParameters = queryMatchResult.parsedParameters,
                    )
                }
            }
        }
    }

// Helpers
// ---------------------------------------------------------------------------------------------------------------------

    private fun matchPath(unmatchedDestination: UnmatchedDestination): PathMatchResult {
        var i = 0
        val pathParameters = buildMap {
            path.forEach { currentPathSegment ->
                val segmentMatchResult = currentPathSegment.matchInDestination(
                    unmatchedDestination.matchablePathSegments,
                    i
                )

                when (segmentMatchResult) {
                    is PathSegment.MatchResult.Mismatch -> {
                        // a path segment completely failed to match, exit early
                        return@matchPath PathMatchResult.Mismatch
                    }

                    is PathSegment.MatchResult.Match -> {
                        // we don't care about the value of this path segment, just continue normally
                        i += segmentMatchResult.numberOfMatchedSegments
                    }

                    is PathSegment.MatchResult.AddParam -> {
                        // the segment matched a parameter value, add it now
                        this[segmentMatchResult.name] = segmentMatchResult.values
                        i += segmentMatchResult.values.size
                    }
                }
            }
        }

        return if (i == (unmatchedDestination.matchablePathSegments.lastIndex + 1)) {
            PathMatchResult.Match(pathParameters)
        } else {
            PathMatchResult.Mismatch
        }
    }

    private fun matchQuery(unmatchedDestination: UnmatchedDestination): QueryMatchResult {
        val filteredQueryParameters = filterQueryParameters(query, unmatchedDestination.matchableQueryParameters)

        return if (filteredQueryParameters.mismatched.isNotEmpty()) {
            // we had some query parameters in the route that did not have values matched in the destination
            QueryMatchResult.Mismatch
        } else if (filteredQueryParameters.unmatched.isNotEmpty()) {
            // we had some query parameters provided through the destination that were not matched to those in the route
            QueryMatchResult.Mismatch
        } else {
            QueryMatchResult.Match(filteredQueryParameters.matched)
        }
    }

    internal fun filterQueryParameters(
        registeredQueryParameters: List<QueryParameter>,
        inputQueryParameters: Map<String, List<String>>,
    ): QueryParameterFilterResults {
        val matchedValues: MutableMap<String, List<String>> = mutableMapOf()
        val unmatchedValues: MutableMap<String, List<String>> = inputQueryParameters.toMutableMap()
        val mismatchedValues: MutableList<QueryParameter> = mutableListOf()

        registeredQueryParameters.forEach { currentQueryParameter ->
            val matchResult = currentQueryParameter.matchInDestination(unmatchedValues.toMap())
            when (matchResult) {
                is QueryParameter.MatchResult.Mismatch -> {
                    // a queryParameter completely failed to match, exit early
                    mismatchedValues += currentQueryParameter
                }

                is QueryParameter.MatchResult.Match -> {
                    // A parameter matched, but didn't need to add any values to the resulting values
                    unmatchedValues.remove(matchResult.name)
                }

                is QueryParameter.MatchResult.AddParams -> {
                    // A parameter matched and added its values to the map
                    matchResult.queryParameters.keys.forEach {
                        unmatchedValues.remove(it)
                    }
                    matchedValues += matchResult.queryParameters
                }
            }
        }

        return QueryParameterFilterResults(
            input = inputQueryParameters,
            matched = matchedValues.toMap(),
            unmatched = unmatchedValues.toMap(),
            mismatched = mismatchedValues.toList(),
        )
    }

    internal data class QueryParameterFilterResults(
        /**
         * All input values provided to the filter
         */
        val input: Map<String, List<String>>,

        /**
         * The values passed through the filter
         */
        val matched: Map<String, List<String>>,

        /**
         * Values passed to the input that were not matched
         */
        val unmatched: Map<String, List<String>>,

        /**
         * Registered query parameters that did not match any values from the input
         */
        val mismatched: List<QueryParameter>,
    )

    private sealed interface PathMatchResult {
        object Mismatch : PathMatchResult
        data class Match(val parsedParameters: Map<String, List<String>>) : PathMatchResult
    }

    private sealed interface QueryMatchResult {
        object Mismatch : QueryMatchResult
        data class Match(val parsedParameters: Map<String, List<String>>) : QueryMatchResult
    }
}
