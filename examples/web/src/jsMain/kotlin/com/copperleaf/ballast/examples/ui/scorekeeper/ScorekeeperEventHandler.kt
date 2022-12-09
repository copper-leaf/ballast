package com.copperleaf.ballast.examples.ui.scorekeeper

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import kotlinx.browser.window

class ScorekeeperEventHandler : EventHandler<
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
            window.alert(event.text)
        }
    }
}
