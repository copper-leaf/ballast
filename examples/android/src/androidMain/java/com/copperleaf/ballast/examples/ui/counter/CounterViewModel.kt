package com.copperleaf.ballast.examples.ui.counter

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.AndroidViewModel
import kotlinx.coroutines.CoroutineScope

class CounterViewModel(
    config: BallastViewModelConfiguration<
        CounterContract.Inputs,
        CounterContract.Events,
        CounterContract.State>,
    coroutineScope: CoroutineScope,
) : AndroidViewModel<
    CounterContract.Inputs,
    CounterContract.Events,
    CounterContract.State>(
    config = config,
    coroutineScope = coroutineScope,
)
