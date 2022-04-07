package com.copperleaf.ballast.examples.kitchensink.controller

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope

class KitchenSinkControllerEventHandler : EventHandler<
    KitchenSinkControllerContract.Inputs,
    KitchenSinkControllerContract.Events,
    KitchenSinkControllerContract.State> {

    override suspend fun EventHandlerScope<
        KitchenSinkControllerContract.Inputs,
        KitchenSinkControllerContract.Events,
        KitchenSinkControllerContract.State>.handleEvent(
        event: KitchenSinkControllerContract.Events
    ) {
    }
}
