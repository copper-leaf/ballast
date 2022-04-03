package com.copperleaf.ballast.debugger.ui.debugger

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.forViewModel
import com.copperleaf.ballast.plusAssign
import com.copperleaf.ballast.savedstate.BallastSavedStateInterceptor
import kotlinx.coroutines.CoroutineScope

class DebuggerViewModel(
    coroutineScope: CoroutineScope,
    configurationBuilder: BallastViewModelConfiguration.Builder,
    inputHandler: DebuggerInputHandler,
    eventHandler: DebuggerEventHandler,
    savedStateAdapter: DebuggerSavedStateAdapter,
) : BasicViewModel<
    DebuggerContract.Inputs,
    DebuggerContract.Events,
    DebuggerContract.State>(
    config = configurationBuilder
        .apply {
            inputStrategy = FifoInputStrategy()
            this += BallastSavedStateInterceptor(savedStateAdapter)
        }
        .forViewModel(
            initialState = DebuggerContract.State(),
            inputHandler = inputHandler,
            name = "Debugger",
        ),
    eventHandler = eventHandler,
    coroutineScope = coroutineScope
)
