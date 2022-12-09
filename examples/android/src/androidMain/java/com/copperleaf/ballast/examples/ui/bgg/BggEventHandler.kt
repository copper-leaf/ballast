package com.copperleaf.ballast.examples.ui.bgg

import androidx.fragment.app.Fragment
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.examples.router.BallastExamplesRouter
import com.copperleaf.ballast.navigation.routing.RouterContract

class BggEventHandler(
    val fragment: Fragment,
    val router: BallastExamplesRouter,
) : EventHandler<
    BggContract.Inputs,
    BggContract.Events,
    BggContract.State> {
    override suspend fun EventHandlerScope<
        BggContract.Inputs,
        BggContract.Events,
        BggContract.State>.handleEvent(
        event: BggContract.Events
    ) = when (event) {
        is BggContract.Events.GoBack -> {
            router.trySend(RouterContract.Inputs.GoBack())
            Unit
        }
    }
}
