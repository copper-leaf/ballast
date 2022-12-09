package com.copperleaf.ballast.examples.ui.counter

import androidx.fragment.app.Fragment
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.examples.router.BallastExamplesRouter
import com.copperleaf.ballast.navigation.routing.RouterContract

class CounterEventHandler(
    val fragment: Fragment,
    val router: BallastExamplesRouter,
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
        is CounterContract.Events.GoBack -> {
            router.trySend(RouterContract.Inputs.GoBack())
            Unit
        }
    }
}
