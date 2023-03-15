package com.copperleaf.ballast.debugger.idea.ui.debugger.server

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.debugger.idea.ui.debugger.vm.DebuggerUiContract
import com.copperleaf.ballast.debugger.idea.ui.debugger.vm.DebuggerUiViewModel
import com.copperleaf.ballast.debugger.server.vm.DebuggerServerContract

class DebuggerServerEventHandler(
    private val getDebuggerUiViewModelLazy: ()->DebuggerUiViewModel
) : EventHandler<
        DebuggerServerContract.Inputs,
        DebuggerServerContract.Events,
        DebuggerServerContract.State> {
    override suspend fun EventHandlerScope<
            DebuggerServerContract.Inputs,
            DebuggerServerContract.Events,
            DebuggerServerContract.State>.handleEvent(
        event: DebuggerServerContract.Events
    ) = when (event) {
        is DebuggerServerContract.Events.ConnectionEstablished -> {
            getDebuggerUiViewModelLazy()
                .send(DebuggerUiContract.Inputs.OnConnectionEstablished(event.connectionId))
        }
    }
}
