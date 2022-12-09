package com.copperleaf.ballast.navigation.internal

import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.Route
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.routing.UnmatchedDestination
import com.copperleaf.ballast.navigation.routing.asMismatchedDestination
import com.copperleaf.ballast.navigation.routing.matchDestinationOrNull

/**
 * A RoutingTable with a statically-defined list of routes, ordered by matcher weight.
 */
internal data class EnumRoutingTable<T>(
    private val routes: List<T>,
) : RoutingTable<T> where T : Enum<T>, T : Route {
    override fun findMatch(
        unmatchedDestination: UnmatchedDestination
    ): Destination<T> {
        return routes
            .firstNotNullOfOrNull { it.matcher.matchDestinationOrNull(it, unmatchedDestination) }
            ?: unmatchedDestination.asMismatchedDestination()
    }
}
