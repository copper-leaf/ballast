package com.copperleaf.ballast.examples.counter

import androidx.compose.material3.SnackbarHostState
import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.debugger.BallastDebuggerInterceptor
import com.copperleaf.ballast.plusAssign
import com.copperleaf.ballast.withViewModel
import kotlinx.coroutines.CoroutineScope

typealias CounterViewModel = BallastViewModel<
        CounterContract.Inputs,
        CounterContract.Events,
        CounterContract.State>

// Build VM
// ---------------------------------------------------------------------------------------------------------------------

internal fun createViewModel(
    viewModelCoroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
): CounterViewModel {
    return BasicViewModel(
        coroutineScope = viewModelCoroutineScope,
        config = BallastViewModelConfiguration.Builder()
            .logging()
            .debugging()
            .withViewModel(
                initialState = CounterContract.State(),
                inputHandler = CounterInputHandler(),
                name = "Counter",
            )
            .build(),
        eventHandler = CounterEventHandler(snackbarHostState),
    )
}

private fun BallastViewModelConfiguration.Builder.logging(): BallastViewModelConfiguration.Builder = apply {
    logger = ::platformLogger
    this += LoggingInterceptor()
}

private fun BallastViewModelConfiguration.Builder.debugging(): BallastViewModelConfiguration.Builder = apply {
    this += BallastDebuggerInterceptor(platformDebuggerConnection())
}
