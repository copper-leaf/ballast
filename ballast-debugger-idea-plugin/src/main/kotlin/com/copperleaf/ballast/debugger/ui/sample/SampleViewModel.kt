package com.copperleaf.ballast.debugger.ui.sample

import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.BaseViewModel
import com.copperleaf.ballast.core.DefaultViewModelConfiguration
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import com.copperleaf.ballast.debugger.BallastDebuggerInterceptor
import kotlinx.coroutines.CoroutineScope

class SampleViewModel(
    viewModelCoroutineScope: CoroutineScope,
    configurationBuilder: DefaultViewModelConfiguration.Builder,
    debuggerConnection: BallastDebuggerClientConnection<*>,
    inputStrategy: InputStrategy,
    inputHandler: SampleInputHandler,
    eventHandler: SampleEventHandler,
) : BaseViewModel<
    SampleContract.Inputs,
    SampleContract.Events,
    SampleContract.State>(
    config = configurationBuilder
        .forViewModel(
            initialState = SampleContract.State(),
            inputHandler = inputHandler
        )
        .apply {
            name = "Sample"
            this.inputStrategy = inputStrategy
            this += BallastDebuggerInterceptor(debuggerConnection)
        }
        .build(),
    eventHandler = eventHandler,
    coroutineScope = viewModelCoroutineScope
)
