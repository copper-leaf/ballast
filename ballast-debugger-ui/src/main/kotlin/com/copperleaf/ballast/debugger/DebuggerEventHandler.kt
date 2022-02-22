package com.copperleaf.ballast.debugger

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import org.slf4j.Logger

class DebuggerEventHandler(
    private val logger: Logger,
) : EventHandler<
        DebuggerContract.Inputs,
        DebuggerContract.Events,
        DebuggerContract.State> {
    override suspend fun EventHandlerScope<
        DebuggerContract.Inputs,
        DebuggerContract.Events,
        DebuggerContract.State>.handleEvent(
        event: DebuggerContract.Events
    ) = when (event) {
        else -> { }
    }
}
