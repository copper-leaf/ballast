package com.copperleaf.ballast.debugger.ui.sample

import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.BaseViewModel
import com.copperleaf.ballast.core.DefaultViewModelConfiguration
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import com.copperleaf.ballast.debugger.BallastDebuggerInterceptor
import kotlinx.coroutines.CoroutineScope

class SampleViewModel(
    applicationCoroutineScope: CoroutineScope,
    viewModelCoroutineScope: CoroutineScope,
    debuggerConnection: BallastDebuggerClientConnection<*>,
    inputStrategy: InputStrategy,
    inputHandler: SampleInputHandler,
    eventHandler: SampleEventHandler,
) : BaseViewModel<
    SampleContract.Inputs,
    SampleContract.Events,
    SampleContract.State>(
    config = DefaultViewModelConfiguration(
        initialState = SampleContract.State(),
        inputHandler = inputHandler,
        inputStrategy = inputStrategy,
        interceptors = listOf(
            BallastDebuggerInterceptor(applicationCoroutineScope, debuggerConnection),
        ),
        name = "Sample",
    ),
    eventHandler = eventHandler,
    coroutineScope = viewModelCoroutineScope
)
