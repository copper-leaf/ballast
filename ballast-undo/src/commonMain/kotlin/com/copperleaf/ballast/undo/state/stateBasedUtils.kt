package com.copperleaf.ballast.undo.state

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.withViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.sample
import kotlin.time.Duration.Companion.seconds

public fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.Builder.withStateBasedUndoController(
    bufferStates: (Flow<State>) -> Flow<State> = { it.sample(5.seconds) },
    historyDepth: Int = 10,
): BallastViewModelConfiguration.Builder {
    return this
        .withViewModel(
            initialState = StateBasedUndoControllerContract.State<Inputs, Events, State>(),
            inputHandler = StateBasedUndoControllerInputHandler<Inputs, Events, State>(bufferStates, historyDepth),
            name = "UndoController",
        )
        .apply {
            this.inputStrategy = FifoInputStrategy()
        }
}
