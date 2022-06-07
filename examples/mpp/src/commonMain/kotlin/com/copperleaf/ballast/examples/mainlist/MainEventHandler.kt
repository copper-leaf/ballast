package com.copperleaf.ballast.examples.mainlist

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.examples.navigation.RouterViewModel
import com.copperleaf.ballast.navigation.routing.RouterContract

class MainEventHandler(
    private val router: RouterViewModel
) : EventHandler<
    MainContract.Inputs,
    MainContract.Events,
    MainContract.State> {
    override suspend fun EventHandlerScope<
        MainContract.Inputs,
        MainContract.Events,
        MainContract.State>.handleEvent(
        event: MainContract.Events
    ) = when (event) {
        is MainContract.Events.NavigateTo -> {
            router.send(RouterContract.Inputs.GoToDestination(event.route.originalRoute))
        }
    }
}
