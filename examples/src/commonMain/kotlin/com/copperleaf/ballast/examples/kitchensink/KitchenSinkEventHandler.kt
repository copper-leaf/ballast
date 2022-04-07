package com.copperleaf.ballast.examples.kitchensink

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import kotlinx.coroutines.delay

class KitchenSinkEventHandler(
    val onWindowClosed: () -> Unit,
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
        is KitchenSinkContract.Events.CloseWindow -> {
            onWindowClosed()
        }
        is KitchenSinkContract.Events.LongRunningEvent -> {
            delay(5000)
        }
        is KitchenSinkContract.Events.ErrorRunningEvent -> {
            error("error running event")
        }
    }
}
