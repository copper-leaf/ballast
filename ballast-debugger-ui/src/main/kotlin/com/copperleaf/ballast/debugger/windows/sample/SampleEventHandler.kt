package com.copperleaf.ballast.debugger.windows.sample

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import kotlinx.coroutines.delay
import org.slf4j.Logger

class SampleEventHandler(
    private val logger: Logger,
    val onWindowClosed: () -> Unit,
) : EventHandler<
        SampleContract.Inputs,
        SampleContract.Events,
        SampleContract.State> {
    override suspend fun EventHandlerScope<
        SampleContract.Inputs,
        SampleContract.Events,
        SampleContract.State>.handleEvent(
        event: SampleContract.Events
    ) = when (event) {
        is SampleContract.Events.CloseWindow -> {
            onWindowClosed()
        }
        is SampleContract.Events.LongRunningEvent -> {
            delay(5000)
        }
        is SampleContract.Events.ErrorRunningEvent -> {
            error("error running event")
        }
    }
}
