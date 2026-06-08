package com.copperleaf.ballast.queue.scope

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.InputStrategyScope
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.internal.BallastViewModelImpl
import kotlinx.coroutines.CoroutineScope

internal class JobQueueInputStrategyScope<Inputs : Any, Events : Any, State : Any>(
    internal val impl: BallastViewModelImpl<Inputs, Events, State>,
    inputStrategyCoroutineScope: CoroutineScope,
) : InputStrategyScope<Inputs, Events, State>,
    CoroutineScope by inputStrategyCoroutineScope {

    override val logger: BallastLogger get() = impl.logger

    override suspend fun acceptQueued(
        queued: Queued<Inputs, Events, State>,
        guardian: InputStrategy.Guardian,
        onCancelled: suspend () -> Unit
    ) {
        impl.inputActor.safelyHandleQueued(queued, guardian, {}, onCancelled)
    }

    override suspend fun acceptQueued(
        queued: Queued<Inputs, Events, State>,
        guardian: InputStrategy.Guardian,
        onFailed: suspend (t: Throwable) -> Unit,
        onCancelled: suspend () -> Unit,
    ) {
        impl.inputActor.safelyHandleQueued(queued, guardian, onFailed, onCancelled)
    }

    override suspend fun getCurrentState(): State {
        throw NotImplementedError("getCurrentState()")
    }

    override suspend fun rollbackState(state: State) {
        throw NotImplementedError("rollbackState()")
    }

    override suspend fun rejectInput(input: Inputs, currentState: State) {
        throw NotImplementedError("rejectInput()")
    }
}
