package com.copperleaf.ballast.debugger.windows.sample

import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.BaseViewModel
import com.copperleaf.ballast.core.DefaultViewModelConfiguration
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import com.copperleaf.ballast.debugger.BallastDebuggerInterceptor
import kotlinx.coroutines.CoroutineScope
import org.slf4j.Logger

class SampleViewModel(
    coroutineScope: CoroutineScope,
    debuggerConnection: BallastDebuggerClientConnection<*>,
    logger: Logger,
    inputStrategy: InputStrategy,
    onWindowClosed: () -> Unit,
) : BaseViewModel<
    SampleContract.Inputs,
    SampleContract.Events,
    SampleContract.State>(
    config = DefaultViewModelConfiguration(
        initialState = SampleContract.State(),
        inputHandler = SampleInputHandler(logger),
        inputStrategy = inputStrategy,
        interceptors = listOf(
            LoggingInterceptor { logger.debug(it) },
            BallastDebuggerInterceptor(debuggerConnection),
        ),
        name = "Sample Window",
    ),
    eventHandler = SampleEventHandler(logger, onWindowClosed),
    coroutineScope = coroutineScope
)
