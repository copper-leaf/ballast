package com.copperleaf.ballast.examples.bgg.ui

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.examples.navigation.RouterViewModel
import com.copperleaf.ballast.navigation.routing.RouterContract

class BggEventHandler(
    private val router: RouterViewModel,
) : EventHandler<
    BggContract.Inputs,
    BggContract.Events,
    BggContract.State> {
    override suspend fun EventHandlerScope<
        BggContract.Inputs,
        BggContract.Events,
        BggContract.State>.handleEvent(
        event: BggContract.Events
    ) = when(event) {
        is BggContract.Events.NavigateBackwards -> {
            router.send(RouterContract.Inputs.GoBack)
        }
    }
}
