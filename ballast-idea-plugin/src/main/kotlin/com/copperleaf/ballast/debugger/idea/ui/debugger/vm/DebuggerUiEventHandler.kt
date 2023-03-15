package com.copperleaf.ballast.debugger.idea.ui.debugger.vm

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope

class DebuggerUiEventHandler() : EventHandler<
        DebuggerUiContract.Inputs,
        DebuggerUiContract.Events,
        DebuggerUiContract.State> {
    override suspend fun EventHandlerScope<
            DebuggerUiContract.Inputs,
            DebuggerUiContract.Events,
            DebuggerUiContract.State>.handleEvent(
        event: DebuggerUiContract.Events
    ) = when (event) {
        else -> {}
    }
}
