package com.copperleaf.ballast.examples.counter

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.examples.navigation.RouterViewModel
import com.copperleaf.ballast.navigation.routing.RouterContract

class CounterEventHandler(
    private val router: RouterViewModel,
) : EventHandler<
    CounterContract.Inputs,
    CounterContract.Events,
    CounterContract.State> {
    override suspend fun EventHandlerScope<
        CounterContract.Inputs,
        CounterContract.Events,
        CounterContract.State>.handleEvent(
        event: CounterContract.Events
    ) = when (event) {
        is CounterContract.Events.NavigateBackwards -> {
            router.send(RouterContract.Inputs.GoBack)
        }
    }
}
