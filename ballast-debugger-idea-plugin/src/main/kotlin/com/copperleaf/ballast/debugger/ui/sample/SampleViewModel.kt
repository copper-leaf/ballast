package com.copperleaf.ballast.debugger.ui.sample

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import com.copperleaf.ballast.debugger.BallastDebuggerInterceptor
import com.copperleaf.ballast.forViewModel
import com.copperleaf.ballast.plusAssign
import kotlinx.coroutines.CoroutineScope

class SampleViewModel(
    viewModelCoroutineScope: CoroutineScope,
    configurationBuilder: BallastViewModelConfiguration.Builder,
    debuggerConnection: BallastDebuggerClientConnection<*>,
    inputStrategy: InputStrategy,
    inputHandler: SampleInputHandler,
    eventHandler: SampleEventHandler,
) : BasicViewModel<
    SampleContract.Inputs,
    SampleContract.Events,
    SampleContract.State>(
    config = configurationBuilder
        .apply {
            this.inputStrategy = inputStrategy
            this += BallastDebuggerInterceptor(debuggerConnection)
        }
        .forViewModel(
            initialState = SampleContract.State(),
            inputHandler = inputHandler,
            name = "Sample",
        ),
    eventHandler = eventHandler,
    coroutineScope = viewModelCoroutineScope
)
