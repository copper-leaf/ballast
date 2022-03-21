package com.copperleaf.ballast.examples.counter

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.forViewModel
import kotlinx.coroutines.CoroutineScope
import kotlin.time.ExperimentalTime

@ExperimentalTime
class CounterViewModel(
    viewModelCoroutineScope: CoroutineScope,
    configurationBuilder: BallastViewModelConfiguration.Builder,
) : BasicViewModel<
    CounterContract.Inputs,
    CounterContract.Events,
    CounterContract.State>(
    config = configurationBuilder
        .forViewModel(
            initialState = CounterContract.State(),
            inputHandler = CounterInputHandler(),
            name = "Counter",
        ),
    eventHandler = CounterEventHandler(),
    coroutineScope = viewModelCoroutineScope,
)
