package com.copperleaf.ballast.navigation.routing

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope

public class RouterInputHandler : InputHandler<
    RouterContract.Inputs,
    RouterContract.Events,
    RouterContract.State> {

    override suspend fun InputHandlerScope<
        RouterContract.Inputs,
        RouterContract.Events,
        RouterContract.State>.handleInput(
        input: RouterContract.Inputs
    ) {
        updateBackstackAndSendNotifications {
            input.updateBackstack(it)
        }
    }

    private suspend fun InputHandlerScope<
        RouterContract.Inputs,
        RouterContract.Events,
        RouterContract.State>.updateBackstackAndSendNotifications(
        updateFn: (RouterContract.State) -> RouterContract.State
    ) {
        val originalState = getCurrentState()

        val updatedState = updateStateAndGet(updateFn)

        if (updatedState.backstack.isEmpty()) {
            if (originalState.backstack.isEmpty()) {
                error("cannot go back, backstack was empty")
            } else {
                postEvent(RouterContract.Events.OnBackstackEmptied)
            }
        } else {
            if (updatedState != originalState) {
                postEvent(
                    RouterContract.Events.NewDestination(
                        backstack = updatedState.backstack,
                        currentDestination = updatedState.currentDestination,
                        currentTag = updatedState.currentTag
                    )
                )
            }
        }
    }
}
