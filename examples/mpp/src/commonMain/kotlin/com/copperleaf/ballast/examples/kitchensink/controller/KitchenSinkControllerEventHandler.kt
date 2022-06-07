package com.copperleaf.ballast.examples.kitchensink.controller

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.examples.navigation.RouterViewModel
import com.copperleaf.ballast.navigation.routing.RouterContract

class KitchenSinkControllerEventHandler(
    private val routerViewModel: RouterViewModel
) : EventHandler<
    KitchenSinkControllerContract.Inputs,
    KitchenSinkControllerContract.Events,
    KitchenSinkControllerContract.State> {

    override suspend fun EventHandlerScope<
        KitchenSinkControllerContract.Inputs,
        KitchenSinkControllerContract.Events,
        KitchenSinkControllerContract.State>.handleEvent(
        event: KitchenSinkControllerContract.Events
    ) = when (event) {
        is KitchenSinkControllerContract.Events.NavigateBackwards -> {
            routerViewModel.send(RouterContract.Inputs.GoBack)
        }
    }
}
