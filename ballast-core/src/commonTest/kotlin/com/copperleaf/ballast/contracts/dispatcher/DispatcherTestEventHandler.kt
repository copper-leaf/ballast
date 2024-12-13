package com.copperleaf.ballast.contracts.dispatcher

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope

public class DispatcherTestEventHandler : EventHandler<
        DispatcherTestContract.Inputs,
        DispatcherTestContract.Events,
        DispatcherTestContract.State> {
    override suspend fun EventHandlerScope<
            DispatcherTestContract.Inputs,
            DispatcherTestContract.Events,
            DispatcherTestContract.State>.handleEvent(
        event: DispatcherTestContract.Events
    ) = when (event) {
        is DispatcherTestContract.Events.GetEventDispatcher -> {
            postInput(DispatcherTestContract.Inputs.SetEventDispatcher(
                actualEventCoroutineScopeInfo = getCoroutineScopeInfo()
            ))
        }
    }
}
