package com.copperleaf.ballast.debugger.ui.debugger

import com.copperleaf.ballast.core.BaseViewModel
import com.copperleaf.ballast.core.DefaultViewModelConfiguration
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.core.LoggingInterceptor
import kotlinx.coroutines.CoroutineScope

class DebuggerViewModel(
    coroutineScope: CoroutineScope,
    inputHandler: DebuggerInputHandler,
    eventHandler: DebuggerEventHandler,
) : BaseViewModel<
    DebuggerContract.Inputs,
    DebuggerContract.Events,
    DebuggerContract.State>(
    config = DefaultViewModelConfiguration(
        initialState = DebuggerContract.State(),
        inputHandler = inputHandler,
        inputStrategy = FifoInputStrategy(),
        name = "Debugger",
    ),
    eventHandler = eventHandler,
    coroutineScope = coroutineScope
)
