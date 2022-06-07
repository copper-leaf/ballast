package com.copperleaf.ballast.navigation.routing

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BootstrapInterceptor
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.plusAssign

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

public fun Route.asStartDestinationString(): String {
    check(this.matcher.path.all { it is PathSegment.Static }) {
        "For a Route to be used as a Start Destination, it must be fully static " +
            "(no path parameters, wildcards, or tailcards)"
    }

    return this.originalRoute
}

public fun Route.asInitialBackstack(): List<Destination> {
    return listOf(this.asStartDestination())
}

public fun BallastViewModelConfiguration.Builder.withRouter(
    navGraph: NavGraph,
    initialRoute: Route = navGraph.routes.first(),
): BallastViewModelConfiguration.Builder =
    this
        .apply {
            this.inputStrategy = FifoInputStrategy()
            this.inputHandler = RouterInputHandler()
            this.initialState = RouterContract.State(navGraph = navGraph)
            this.name = "Router"

            this += BootstrapInterceptor {
                RouterContract.Inputs.GoToDestination(
                    initialRoute.asStartDestinationString()
                )
            }
        }
