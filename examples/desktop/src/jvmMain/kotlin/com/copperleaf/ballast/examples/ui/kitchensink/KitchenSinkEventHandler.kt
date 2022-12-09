package com.copperleaf.ballast.examples.ui.kitchensink

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.examples.router.BallastExamples
import com.copperleaf.ballast.navigation.routing.RouterContract
import com.copperleaf.ballast.navigation.vm.Router
import kotlinx.coroutines.delay

class KitchenSinkEventHandler(
    private val router: Router<BallastExamples>
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
        }

        is KitchenSinkContract.Events.NavigateTo -> {
            router.trySend(RouterContract.Inputs.GoToDestination(event.directions))
            Unit
        }

        is KitchenSinkContract.Events.LongRunningEvent -> {
            delay(5000)
        }

        is KitchenSinkContract.Events.ErrorRunningEvent -> {
            error("error running event")
        }
    }
}
