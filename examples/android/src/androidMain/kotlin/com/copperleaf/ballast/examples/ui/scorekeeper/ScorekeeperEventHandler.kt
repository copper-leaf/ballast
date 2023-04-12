package com.copperleaf.ballast.examples.ui.scorekeeper

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.examples.router.BallastExamplesRouter
import com.copperleaf.ballast.navigation.routing.RouterContract

class ScorekeeperEventHandler(
    val fragment: Fragment,
    val router: BallastExamplesRouter,
) : EventHandler<
    ScorekeeperContract.Inputs,
    ScorekeeperContract.Events,
    ScorekeeperContract.State> {
    override suspend fun EventHandlerScope<
        ScorekeeperContract.Inputs,
        ScorekeeperContract.Events,
        ScorekeeperContract.State>.handleEvent(
        event: ScorekeeperContract.Events
    ) = when (event) {
        is ScorekeeperContract.Events.GoBack -> {
            router.trySend(RouterContract.Inputs.GoBack())
            Unit
        }

        is ScorekeeperContract.Events.ShowErrorMessage -> {
            Toast.makeText(fragment.requireContext(), event.text, Toast.LENGTH_SHORT).show()
        }

    }
}
