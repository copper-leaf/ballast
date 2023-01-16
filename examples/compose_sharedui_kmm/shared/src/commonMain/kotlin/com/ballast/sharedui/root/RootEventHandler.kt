package com.ballast.sharedui.root

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope

class RootEventHandler :
    EventHandler<RootContract.Inputs, RootContract.Events, RootContract.State> {
    override suspend fun EventHandlerScope<RootContract.Inputs, RootContract.Events, RootContract.State>.handleEvent(
        event: RootContract.Events,
    ) {

    }
}
