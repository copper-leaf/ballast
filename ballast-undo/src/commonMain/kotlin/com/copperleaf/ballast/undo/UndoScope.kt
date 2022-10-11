package com.copperleaf.ballast.undo

import com.copperleaf.ballast.ExperimentalBallastApi
import kotlinx.coroutines.CoroutineScope

@ExperimentalBallastApi
public interface UndoScope<Inputs : Any, Events : Any, State : Any> : CoroutineScope {
    public suspend fun restoreState(state: State)
}
