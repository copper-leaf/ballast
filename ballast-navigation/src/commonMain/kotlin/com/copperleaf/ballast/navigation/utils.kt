package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.MissingDestination
import com.copperleaf.ballast.navigation.routing.NavToken
import com.copperleaf.ballast.navigation.routing.PathSegment
import com.copperleaf.ballast.navigation.routing.Route
import com.copperleaf.ballast.navigation.routing.matchDestinationOrThrow

public val RouterContract.State.currentDestination: Destination?
    get() {
        return backstack
            .lastOrNull {
                it is Destination
            }
            as? Destination?
    }

public val RouterContract.State.currentDestinationOrNotFound: NavToken?
    get() {
        return backstack.lastOrNull {
            when (it) {
                is Destination -> true
                is MissingDestination -> true
                else -> false
            }
        }
    }

public fun Route.asStartDestination(): Destination {
    check(this.matcher.path.all { it is PathSegment.Static }) {
        "For a Route to be used as a Start Destination, it must be fully static " +
            "(no path parameters, wildcards, or tailcards)"
    }

    return this.matchDestinationOrThrow(originalRoute)
}

public fun Route.asInitialBackstack(): List<Destination> {
    return listOf(this.asStartDestination())
}
