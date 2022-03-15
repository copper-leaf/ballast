package com.copperleaf.ballast.debugger.ui.debugger

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.forViewModel
import kotlinx.coroutines.CoroutineScope

class DebuggerViewModel(
    coroutineScope: CoroutineScope,
    configurationBuilder: BallastViewModelConfiguration.Builder,
    inputHandler: DebuggerInputHandler,
    eventHandler: DebuggerEventHandler,
) : BasicViewModel<
    DebuggerContract.Inputs,
    DebuggerContract.Events,
    DebuggerContract.State>(
    config = configurationBuilder
        .apply {
            inputStrategy = FifoInputStrategy()
        }
        .forViewModel(
            initialState = DebuggerContract.State(),
            inputHandler = inputHandler,
            name = "Debugger",
        ),
    eventHandler = eventHandler,
    coroutineScope = coroutineScope
)
