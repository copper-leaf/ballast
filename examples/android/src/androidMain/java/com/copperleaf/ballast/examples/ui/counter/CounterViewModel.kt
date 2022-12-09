package com.copperleaf.ballast.examples.ui.counter

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.ExperimentalBallastApi
import com.copperleaf.ballast.core.AndroidViewModel

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
