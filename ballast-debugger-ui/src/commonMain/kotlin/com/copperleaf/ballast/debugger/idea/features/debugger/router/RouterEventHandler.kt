package com.copperleaf.ballast.debugger.idea.features.debugger.router

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.navigation.routing.RouterContract

public class RouterEventHandler : EventHandler<
        RouterContract.Inputs<DebuggerRoute>,
        RouterContract.Events<DebuggerRoute>,
        RouterContract.State<DebuggerRoute>> {
    override suspend fun EventHandlerScope<
            RouterContract.Inputs<DebuggerRoute>,
            RouterContract.Events<DebuggerRoute>,
            RouterContract.State<DebuggerRoute>>.handleEvent(
        event: RouterContract.Events<DebuggerRoute>
    ): Unit = when (event) {
        is RouterContract.Events.BackstackChanged -> {}
        is RouterContract.Events.BackstackEmptied -> {}
        is RouterContract.Events.NoChange -> {}
    }
}
