package com.copperleaf.ballast.examples.ui.bgg

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope

class BggEventHandler : EventHandler<
    BggContract.Inputs,
    BggContract.Events,
    BggContract.State> {
    override suspend fun EventHandlerScope<
        BggContract.Inputs,
        BggContract.Events,
        BggContract.State>.handleEvent(
        event: BggContract.Events
    ) {
    }
}
