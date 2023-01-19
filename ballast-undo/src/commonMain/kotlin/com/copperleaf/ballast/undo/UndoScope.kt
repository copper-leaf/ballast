package com.copperleaf.ballast.undo

import kotlinx.coroutines.CoroutineScope

public interface UndoScope<Inputs : Any, Events : Any, State : Any> : CoroutineScope {
    public suspend fun restoreState(state: State)
}
