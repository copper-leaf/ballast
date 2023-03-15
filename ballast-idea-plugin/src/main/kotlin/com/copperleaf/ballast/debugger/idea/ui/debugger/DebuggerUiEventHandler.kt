package com.copperleaf.ballast.debugger.idea.ui.debugger

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.debugger.idea.ui.debugger.router.DebuggerRouter
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerViewModel

class DebuggerUiEventHandler(
    private val getDebuggerRouterLazy: ()->DebuggerRouter,
    private val getDebuggerServerViewModelLazy: ()->DebuggerServerViewModel,
) : EventHandler<
        DebuggerUiContract.Inputs,
        DebuggerUiContract.Events,
        DebuggerUiContract.State> {
    override suspend fun EventHandlerScope<
            DebuggerUiContract.Inputs,
            DebuggerUiContract.Events,
            DebuggerUiContract.State>.handleEvent(
        event: DebuggerUiContract.Events
    ) = when (event) {
        is DebuggerUiContract.Events.SendCommandToRouter -> {
            getDebuggerRouterLazy().send(event.input)
        }

        is DebuggerUiContract.Events.SendCommandToDebuggerServer -> {
            getDebuggerServerViewModelLazy().send(event.input)
        }
    }
}
