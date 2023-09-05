package com.copperleaf.ballast.examples.router

import com.copperleaf.ballast.examples.preferences.BallastExamplesPreferences
import com.copperleaf.ballast.navigation.routing.Route
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.isStatic
import com.copperleaf.ballast.savedstate.RestoreStateScope
import com.copperleaf.ballast.savedstate.SaveStateScope
import com.copperleaf.ballast.savedstate.SavedStateAdapter

/**
 * Automatically save and restore the state of the Router with any route changes. Do not pass an initial route to the
 * BallastViewModelConfiguration.Builder.withRouter()` when using this adapter, as it will handle setting the initial
 * route instead, and may conflict with the initial route set through that function.
 *
 * The actual serialization and persistence of the backstack is delegated through [prefs].
 *
 * If you are also using the Ballast Undo/Redo module for forward/backward navigation, set [preserveDiscreteStates] to
 * true so the backstack is restored through individual [RouterContract.Inputs.GoToDestination] Inputs to capture each
 * intermediate state. If not, it can be set to false so that a single [RouterContract.Inputs.RestoreBackstack] is used
 * instead.
 */
public class RouterSavedStateAdapter<T : Route>(
    private val routingTable: RoutingTable<T>,
    private val initialRoute: T?,
    private val prefs: BallastExamplesPreferences,
    private val preserveDiscreteStates: Boolean = false,
) : SavedStateAdapter<
        RouterContract.Inputs<T>,
        RouterContract.Events<T>,
        RouterContract.State<T>> {

    override suspend fun SaveStateScope<
            RouterContract.Inputs<T>,
            RouterContract.Events<T>,
            RouterContract.State<T>>.save() {
        saveAll { backstack ->
            prefs.backstack = backstack.map { it.originalDestinationUrl }.takeLast(5)
        }
    }

    override suspend fun RestoreStateScope<
            RouterContract.Inputs<T>,
            RouterContract.Events<T>,
            RouterContract.State<T>
            >.restore(): RouterContract.State<T> {
        val savedBackstack = prefs.backstack
        if(savedBackstack.isEmpty()) {
            initialRoute?.let { initialRoute ->
                check(initialRoute.isStatic()) {
                    "For a Route to be used as a Start Destination, it must be fully static. All path segments and " +
                            "declared query parameters must either be static or optional."
                }
                postInput(
                    RouterContract.Inputs.GoToDestination<T>(initialRoute.directions().build())
                )
            }
        } else if(preserveDiscreteStates) {
            savedBackstack.forEach { destinationUrl ->
                postInput(
                    RouterContract.Inputs.GoToDestination(destinationUrl)
                )
            }
        } else {
            postInput(
                RouterContract.Inputs.RestoreBackstack(savedBackstack)
            )
        }

        return RouterContract.State(routingTable = routingTable)
    }
}

