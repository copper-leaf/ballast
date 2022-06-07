package com.copperleaf.ballast.examples.kitchensink

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.examples.navigation.RouterViewModel
import com.copperleaf.ballast.navigation.routing.RouterContract
import kotlinx.coroutines.delay

class KitchenSinkEventHandler(
    private val router: RouterViewModel,
) : EventHandler<
    KitchenSinkContract.Inputs,
    KitchenSinkContract.Events,
    KitchenSinkContract.State> {
    override suspend fun EventHandlerScope<
        KitchenSinkContract.Inputs,
        KitchenSinkContract.Events,
        KitchenSinkContract.State>.handleEvent(
        event: KitchenSinkContract.Events
    ) = when (event) {
        is KitchenSinkContract.Events.LongRunningEvent -> {
            delay(5000)
        }
        is KitchenSinkContract.Events.ErrorRunningEvent -> {
            error("error running event")
        }
        is KitchenSinkContract.Events.NavigateBackwards -> {
            router.send(RouterContract.Inputs.GoBack)
        }
    }
}
