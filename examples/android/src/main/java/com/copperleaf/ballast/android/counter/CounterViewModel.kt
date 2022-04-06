package com.copperleaf.ballast.android.counter

import androidx.lifecycle.SavedStateHandle
import com.copperleaf.ballast.android.util.commonBuilder
import com.copperleaf.ballast.core.AndroidViewModel
import com.copperleaf.ballast.examples.counter.CounterContract
import com.copperleaf.ballast.examples.counter.CounterInputHandler
import com.copperleaf.ballast.forViewModel
import com.copperleaf.ballast.plusAssign
import com.copperleaf.ballast.savedstate.BallastSavedStateInterceptor

class CounterViewModel(
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel<
    CounterContract.Inputs,
    CounterContract.Events,
    CounterContract.State>(
    config = commonBuilder()
        .apply {
            this += BallastSavedStateInterceptor(
                CounterSavedStateAdapter(savedStateHandle)
            )
        }
        .forViewModel(
            initialState = CounterContract.State(),
            inputHandler = CounterInputHandler(),
            name = "Counter",
        ),
)
