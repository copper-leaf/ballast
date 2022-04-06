package com.copperleaf.ballast.examples.web.counter

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.forViewModel
import kotlinx.coroutines.CoroutineScope
import com.copperleaf.ballast.examples.counter.CounterContract
import com.copperleaf.ballast.examples.counter.CounterInputHandler
import com.copperleaf.ballast.examples.counter.CounterEventHandler

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
