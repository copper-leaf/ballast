package com.copperleaf.ballast.contracts.dispatcher

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.coroutineContext

@OptIn(ExperimentalStdlibApi::class)
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
                actualEventDispatcher = coroutineContext[CoroutineDispatcher]
            ))
        }
    }
}
