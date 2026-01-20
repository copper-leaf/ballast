package com.copperleaf.ballast.queue

import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.InputStrategyScope
import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.internal.BallastViewModelImpl
import com.copperleaf.ballast.internal.actors.StateActor
import com.copperleaf.ballast.internal.scopes.DefaultBallastScopeFactory
import com.copperleaf.ballast.internal.scopes.InternalInputHandlerScope
import com.copperleaf.ballast.queue.scope.JobQueueInputHandlerScope
import com.copperleaf.ballast.queue.scope.JobQueueInputStrategyScope
import com.copperleaf.ballast.queue.scope.JobQueueSideJobScope
import com.copperleaf.ballast.queue.scope.JobQueueStateActor
import kotlinx.coroutines.CoroutineScope

@Suppress("UNCHECKED_CAST")
internal class JobQueueScopeFactory<Inputs : Any, Events : Any, State : Any>(
    impl: BallastViewModelImpl<Inputs, Events, State>
) : DefaultBallastScopeFactory<Inputs, Events, State>(impl) {

    override fun createInputHandlerScope(
        guardian: InputStrategy.Guardian,
    ): InternalInputHandlerScope<Inputs, Events, State> = with(impl) {
        require(guardian is JobQueueGuardian<*, *>)
        return JobQueueInputHandlerScope(
            guardian = guardian as JobQueueGuardian<Events, State>,
            impl = impl,
        )
    }

    override fun createStateActor(impl: BallastViewModelImpl<Inputs, Events, State>): StateActor<Inputs, Events, State> {
        return JobQueueStateActor()
    }

    override fun createInputStrategyScope(inputStrategyCoroutineScope: CoroutineScope): InputStrategyScope<Inputs, Events, State> {
        return JobQueueInputStrategyScope(
            impl = impl,
            inputStrategyCoroutineScope = inputStrategyCoroutineScope,
        )
    }

    override fun createSideJobScope(
        sideJobCoroutineScope: CoroutineScope,
        key: String,
        restartState: SideJobScope.RestartState
    ): SideJobScope<Inputs, Events, State> = with(impl) {
        JobQueueSideJobScope(
            sideJobCoroutineScope = sideJobCoroutineScope,
            logger = logger,
            inputActor = inputActor,
            interceptorActor = interceptorActor,
            key = key,
            restartState = restartState,
            shutDownGracePeriod = shutDownGracePeriod,
        )
    }
}
