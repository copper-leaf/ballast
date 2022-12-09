package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.ExperimentalBallastApi
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.Route
import com.copperleaf.ballast.navigation.routing.RouteAnnotation
import com.copperleaf.ballast.navigation.routing.RouteMatcher
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.routing.UnmatchedDestination
import com.copperleaf.ballast.navigation.routing.asMismatchedDestination
import com.copperleaf.ballast.navigation.routing.matchDestinationOrNull

public data class SimpleRoute(
    override val matcher: RouteMatcher,
    override val annotations: Set<RouteAnnotation> = emptySet(),
) : Route {
    constructor(routeFormat: String) : this(RouteMatcher.create(routeFormat))
    constructor(routeFormat: String, weight: Double) : this(RouteMatcher.create(routeFormat) { _, _ -> weight })

    override fun toString(): String {
        return matcher.routeFormat
    }
}

@OptIn(ExperimentalBallastApi::class)
public class SimpleRoutingTable(
    vararg routes: SimpleRoute,
) : RoutingTable<SimpleRoute> {
    private val routes: List<SimpleRoute> = routes.sortedByDescending { it.matcher.weight }

    override fun findMatch(
        unmatchedDestination: UnmatchedDestination
    ): Destination<SimpleRoute> {
        return routes
            .firstNotNullOfOrNull { it.matcher.matchDestinationOrNull(it, unmatchedDestination) }
            ?: unmatchedDestination.asMismatchedDestination()
    }
}
