package com.copperleaf.ballast.examples.counter

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.AndroidViewModel
import com.copperleaf.ballast.examples.counter.CounterContract

class CounterViewModel(
    config: BallastViewModelConfiguration<
        CounterContract.Inputs,
        CounterContract.Events,
        CounterContract.State>
) : AndroidViewModel<
    CounterContract.Inputs,
    CounterContract.Events,
    CounterContract.State>(
    config = config
)
