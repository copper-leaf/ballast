package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.navigation.routing.BackstackNavigator
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.Route
import com.copperleaf.ballast.navigation.routing.RouteAnnotation
import com.copperleaf.ballast.navigation.routing.RouteMatcher
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.routing.UnmatchedDestination
import com.copperleaf.ballast.navigation.routing.asMismatchedDestination
import com.copperleaf.ballast.navigation.routing.matchDestinationOrNull

public data class SimpleRoute(
    override val matcher: RouteMatcher,
    override val annotations: Set<RouteAnnotation> = emptySet(),
) : Route {
    constructor(routeFormat: String) : this(RouteMatcher.create(routeFormat))
    constructor(routeFormat: String, annotations: Set<RouteAnnotation>) : this(RouteMatcher.create(routeFormat), annotations)
    constructor(routeFormat: String, weight: Double) : this(RouteMatcher.create(routeFormat) { _, _ -> weight })

    override fun toString(): String {
        return matcher.routeFormat
    }
}

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

public class MatchAllRoutingTable : RoutingTable<SimpleRoute> {

    override fun findMatch(
        unmatchedDestination: UnmatchedDestination
    ): Destination<SimpleRoute> {
        val url = unmatchedDestination.originalDestinationUrl
        return Destination.Match(url, SimpleRoute(url), annotations = unmatchedDestination.extraAnnotations)
    }
}


public class Navigate<T : Route>(
    private val block: BackstackNavigator<T>.() -> Unit
) : RouterContract.Inputs<T>() {
    override fun BackstackNavigator<T>.navigate() {
        block()
    }

    override fun toString(): String {
        return "GoBack()"
    }
}
