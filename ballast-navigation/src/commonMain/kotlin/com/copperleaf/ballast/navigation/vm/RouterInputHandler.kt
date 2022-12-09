package com.copperleaf.ballast.navigation.vm

import com.copperleaf.ballast.ExperimentalBallastApi
import com.copperleaf.ballast.navigation.internal.BackstackNavigatorImpl
import com.copperleaf.ballast.navigation.routing.Route
import com.copperleaf.ballast.navigation.routing.RouterContract

@ExperimentalBallastApi
public class RouterInputHandlerImpl<T : Route> : RouterInputHandler<T> {

    override suspend fun RouterInputHandlerScope<T>.handleInput(
        input: RouterContract.Inputs<T>
    ) {
        // capture the original backstack state so we can compare changes
        val originalState = getCurrentState()

        // allow the input to modify the backstack as needed
        val updatedState = updateStateAndGet {
            BackstackNavigatorImpl(it, input).applyUpdate()
        }

        // compare the updates to the original backstack, and send the appropriate Events for router clients that react
        // to the backstack changes with one-time changes, rather than passively observing state
        postEvent(
            if (updatedState.backstack.isEmpty()) {
                if (originalState.backstack.isEmpty()) {
                    error("cannot go back, backstack was empty")
                } else {
                    RouterContract.Events.BackstackEmptied()
                }
            } else {
                if (updatedState != originalState) {
                    RouterContract.Events.BackstackChanged(
                        backstack = updatedState.backstack,
                    )
                } else {
                    RouterContract.Events.NoChange()
                }
            }
        )
    }
}
