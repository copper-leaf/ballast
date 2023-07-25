package com.copperleaf.ballast.internal.scopes

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.InputStrategyScope
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.core.LifoInputStrategy
import com.copperleaf.ballast.core.ParallelInputStrategy
import com.copperleaf.ballast.internal.actors.InputActor
import com.copperleaf.ballast.internal.actors.InterceptorActor
import com.copperleaf.ballast.internal.actors.StateActor
import kotlinx.coroutines.CoroutineScope

/**
 * Ballast ViewModels are designed to be safe and prevent you from doing things that could cause hard-to-debug race
 * conditions and break the purity of the MVI "state machine". But there are several ways to do this safely, though each
 * has their own set of pros/cons. By providing a different InputStrategy to your Ballast ViewModels, you can choose
 * which set of tradeoffs you are willing to accept, or you can define your own strategy customized to your needs.
 *
 * See the following links for the available core input strategies. By default, [LifoInputStrategy], which is suitable
 * for UI-bound ViewModels.
 *
 * @see [LifoInputStrategy]
 * @see [FifoInputStrategy]
 * @see [ParallelInputStrategy]
 */
internal class InputStrategyScopeImpl<Inputs : Any, Events : Any, State : Any>(
    inputStrategyCoroutineScope: CoroutineScope,

    override val logger: BallastLogger,
    private val hostViewModelType: String,
    private val hostViewModelName: String,

    private val inputActor: InputActor<Inputs, Events, State>,
    private val stateActor: StateActor<Inputs, Events, State>,
    private val interceptorActor: InterceptorActor<Inputs, Events, State>,
) : InputStrategyScope<Inputs, Events, State>,
    CoroutineScope by inputStrategyCoroutineScope {

    override suspend fun acceptQueued(
        queued: Queued<Inputs, Events, State>,
        guardian: InputStrategy.Guardian,
        onCancelled: suspend () -> Unit
    ) {
        inputActor.safelyHandleQueued(queued, guardian, onCancelled)
    }

    override suspend fun getCurrentState(): State {
        return stateActor.getCurrentState()
    }

    override suspend fun rollbackState(state: State) {
        stateActor.safelySetState(state, null)
    }

    override suspend fun rejectInput(input: Inputs, currentState: State) {
        interceptorActor.notify(
            BallastNotification.InputRejected(
                hostViewModelType,
                hostViewModelName,
                currentState,
                input
            )
        )
    }
}
