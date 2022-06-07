package com.copperleaf.ballast.examples.scorekeeper

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.examples.navigation.RouterViewModel
import com.copperleaf.ballast.navigation.routing.RouterContract

class ScorekeeperEventHandler(
    private val router: RouterViewModel,
    private val displayErrorMessage: suspend (String) -> Unit
) : EventHandler<
    ScorekeeperContract.Inputs,
    ScorekeeperContract.Events,
    ScorekeeperContract.State> {
    override suspend fun EventHandlerScope<
        ScorekeeperContract.Inputs,
        ScorekeeperContract.Events,
        ScorekeeperContract.State>.handleEvent(
        event: ScorekeeperContract.Events
    ) = when (event) {
        is ScorekeeperContract.Events.ShowErrorMessage -> {
            displayErrorMessage(event.text)
        }
        is ScorekeeperContract.Events.NavigateBackwards -> {
            router.send(RouterContract.Inputs.GoBack)
        }
    }
}
