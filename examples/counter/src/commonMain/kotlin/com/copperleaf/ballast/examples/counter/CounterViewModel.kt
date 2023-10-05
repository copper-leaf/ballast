package com.copperleaf.ballast.examples.counter

import com.copperleaf.ballast.BallastViewModel

typealias CounterViewModel = BallastViewModel<
        CounterContract.Inputs,
        CounterContract.Events,
        CounterContract.State>
