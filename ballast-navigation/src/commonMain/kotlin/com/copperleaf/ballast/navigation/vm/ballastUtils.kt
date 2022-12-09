package com.copperleaf.ballast.navigation.vm

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.ExperimentalBallastApi
import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.BootstrapInterceptor
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.navigation.routing.Route
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.routing.RoutingTable
import com.copperleaf.ballast.navigation.routing.build
import com.copperleaf.ballast.navigation.routing.directions
import com.copperleaf.ballast.navigation.routing.isStatic
import com.copperleaf.ballast.plusAssign
import com.copperleaf.ballast.withViewModel

// Configure Router ViewModel
// ---------------------------------------------------------------------------------------------------------------------

/**
 * Configure a ViewModel to be used as a Router. If [initialRoute] is provided, it will be automatically set as the
 * initial route using a [BootstrapInterceptor]. You may wish to keep this value as `null` to load the initial route
 * from some other means.
 */
@ExperimentalBallastApi
public fun <T : Route> BallastViewModelConfiguration.Builder.withRouter(
    routingTable: RoutingTable<T>,
    initialRoute: T?,
): BallastViewModelConfiguration.Builder {
    return this
        .withViewModel(
            initialState = RouterContract.State(routingTable = routingTable),
            inputHandler = RouterInputHandlerImpl(),
            name = "Router",
        )
        .apply {
            this.inputStrategy = FifoInputStrategy()

            initialRoute?.let { initialRoute ->
                check(initialRoute.isStatic()) {
                    "For a Route to be used as a Start Destination, it must be fully static. All path segments and " +
                            "declared query parameters must either be static or optional."
                }

                this += BootstrapInterceptor {
                    RouterContract.Inputs.GoToDestination<T>(initialRoute.directions().build())
                }
            }
        }
}

// Aliases to Ballast classes
// ---------------------------------------------------------------------------------------------------------------------

public typealias Router<T> = BallastViewModel<
        RouterContract.Inputs<T>,
        RouterContract.Events<T>,
        RouterContract.State<T>>

public typealias BasicRouter<T> = BasicViewModel<
        RouterContract.Inputs<T>,
        RouterContract.Events<T>,
        RouterContract.State<T>>

public typealias RouterInputHandler<T> = InputHandler<
        RouterContract.Inputs<T>,
        RouterContract.Events<T>,
        RouterContract.State<T>>

public typealias RouterInputHandlerScope<T> = InputHandlerScope<
        RouterContract.Inputs<T>,
        RouterContract.Events<T>,
        RouterContract.State<T>>

public typealias RouterEventHandler<T> = EventHandler<
        RouterContract.Inputs<T>,
        RouterContract.Events<T>,
        RouterContract.State<T>>

public typealias RouterEventHandlerScope<T> = EventHandlerScope<
        RouterContract.Inputs<T>,
        RouterContract.Events<T>,
        RouterContract.State<T>>

public typealias RouterInterceptor<T> = BallastInterceptor<
        RouterContract.Inputs<T>,
        RouterContract.Events<T>,
        RouterContract.State<T>>

public typealias RouterInterceptorScope<T> = BallastInterceptorScope<
        RouterContract.Inputs<T>,
        RouterContract.Events<T>,
        RouterContract.State<T>>

public typealias RouterNotification<T> = BallastNotification<
        RouterContract.Inputs<T>,
        RouterContract.Events<T>,
        RouterContract.State<T>>
