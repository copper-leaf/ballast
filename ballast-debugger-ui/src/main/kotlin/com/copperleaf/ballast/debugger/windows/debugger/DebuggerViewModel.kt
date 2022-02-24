package com.copperleaf.ballast.debugger.windows.debugger

import com.copperleaf.ballast.core.BaseViewModel
import com.copperleaf.ballast.core.DefaultViewModelConfiguration
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.core.LoggingInterceptor
import kotlinx.coroutines.CoroutineScope
import org.slf4j.Logger

class DebuggerViewModel(
    coroutineScope: CoroutineScope,
    logger: Logger,
) : BaseViewModel<
    DebuggerContract.Inputs,
    DebuggerContract.Events,
    DebuggerContract.State>(
    config = DefaultViewModelConfiguration(
        initialState = DebuggerContract.State(),
        inputHandler = DebuggerInputHandler(logger),
        inputStrategy = FifoInputStrategy(),
        interceptors = listOf(LoggingInterceptor { logger.debug(it) })
    ),
    eventHandler = DebuggerEventHandler(logger),
    coroutineScope = coroutineScope
)
