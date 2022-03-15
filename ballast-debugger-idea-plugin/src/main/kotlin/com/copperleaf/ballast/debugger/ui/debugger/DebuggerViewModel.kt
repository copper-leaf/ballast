package com.copperleaf.ballast.debugger.ui.debugger

import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.DefaultViewModelConfiguration
import com.copperleaf.ballast.core.FifoInputStrategy
import kotlinx.coroutines.CoroutineScope

class DebuggerViewModel constructor(
    coroutineScope: CoroutineScope,
    configurationBuilder: DefaultViewModelConfiguration.Builder,
    inputHandler: DebuggerInputHandler,
    eventHandler: DebuggerEventHandler,
) : BasicViewModel<
    DebuggerContract.Inputs,
    DebuggerContract.Events,
    DebuggerContract.State>(
    config = configurationBuilder
        .forViewModel(
            initialState = DebuggerContract.State(),
            inputHandler = inputHandler
        )
        .apply {
            name = "Debugger"
            inputStrategy = FifoInputStrategy()
        }
        .build(),
    eventHandler = eventHandler,
    coroutineScope = coroutineScope
)
