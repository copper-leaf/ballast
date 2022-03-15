package com.copperleaf.ballast.debugger.ui.debugger

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope

class DebuggerEventHandler : EventHandler<
    DebuggerContract.Inputs,
    DebuggerContract.Events,
    DebuggerContract.State> {
    override suspend fun EventHandlerScope<
        DebuggerContract.Inputs,
        DebuggerContract.Events,
        DebuggerContract.State>.handleEvent(
        event: DebuggerContract.Events
    ) {
    }
}
