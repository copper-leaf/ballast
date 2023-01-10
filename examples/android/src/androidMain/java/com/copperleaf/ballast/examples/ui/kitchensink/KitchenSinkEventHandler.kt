package com.copperleaf.ballast.examples.ui.kitchensink

import androidx.fragment.app.Fragment
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.examples.router.BallastExamplesRouter
import com.copperleaf.ballast.navigation.routing.RouterContract
import kotlinx.coroutines.delay

class KitchenSinkEventHandler(
    val fragment: Fragment,
    val router: BallastExamplesRouter,
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
            router.trySend(RouterContract.Inputs.GoBack())
            Unit
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
