package com.copperleaf.ballast.examples.ui.counter

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import kotlinx.coroutines.CoroutineScope

class CounterViewModel(
    viewModelCoroutineScope: CoroutineScope,
    config: BallastViewModelConfiguration<
        CounterContract.Inputs,
        CounterContract.Events,
        CounterContract.State>,
    eventHandler: CounterEventHandler
) : BasicViewModel<
    CounterContract.Inputs,
    CounterContract.Events,
    CounterContract.State>(
    config = config,
    eventHandler = eventHandler,
    coroutineScope = viewModelCoroutineScope,
)
