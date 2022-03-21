package com.copperleaf.ballast.examples.scorekeeper

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope

class ScorekeeperEventHandler(
    private val displayErrorMessage: (String) -> Unit
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
    }
}
