package com.copperleaf.ballast.navigation.internal

import com.copperleaf.ballast.navigation.routing.Backstack
import com.copperleaf.ballast.navigation.routing.BackstackNavigator
import com.copperleaf.ballast.navigation.routing.Destination
import com.copperleaf.ballast.navigation.routing.Route
import com.copperleaf.ballast.navigation.routing.RouteAnnotation
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.UnmatchedDestination

internal class BackstackNavigatorImpl<T : Route>(
    private var currentState: RouterContract.State<T>,
    private val currentInput: RouterContract.Inputs<T>,
) : BackstackNavigator<T> {
    override val backstack: Backstack<T>
        get() = currentState.backstack

    override fun updateBackstack(block: (Backstack<T>) -> Backstack<T>) {
        currentState = currentState.copy(
            backstack = block(currentState.backstack)
        )
    }

    override fun matchDestination(destinationUrl: String, extraAnnotations: Set<RouteAnnotation>): Destination<T> {
        return currentState.routingTable.findMatch(
            UnmatchedDestination.parse(destinationUrl, extraAnnotations)
        )
    }

    internal fun applyUpdate(): RouterContract.State<T> {
        with(currentInput) {
            navigate()
        }

        return currentState
    }
}
