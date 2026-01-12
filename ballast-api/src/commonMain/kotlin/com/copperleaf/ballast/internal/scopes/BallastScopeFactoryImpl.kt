package com.copperleaf.ballast.internal.scopes

import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastScopeFactory
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventStrategyScope
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.InputStrategyScope
import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.internal.BallastViewModelImpl
import com.copperleaf.ballast.internal.actors.StateActor
import com.copperleaf.ballast.internal.actors.StateActorImpl
import kotlinx.coroutines.CoroutineScope

public open class DefaultBallastScopeFactory<Inputs : Any, Events : Any, State : Any>(
    protected val impl: BallastViewModelImpl<Inputs, Events, State>,
) : BallastScopeFactory<Inputs, Events, State> {

    override fun createBallastInterceptorScope(
        interceptorCoroutineScope: CoroutineScope,
    ): BallastInterceptorScope<Inputs, Events, State> = with(impl) {
        return BallastInterceptorScopeImpl(
            interceptorCoroutineScope = interceptorCoroutineScope,
            logger = logger,
            hostViewModelName = name,
            hostViewModelType = type,
            initialState = initialState,
            inputActor = inputActor,
            eventActor = eventActor,
            encoder = encoder,
            decoder = decoder,
        )
    }

    override fun createEventHandlerScope(): InternalEventHandlerScope<Inputs, Events, State> = with(impl) {
        return EventHandlerScopeImpl(
            logger = logger,
            inputActor = inputActor,
            interceptorActor = interceptorActor,
        )
    }

    override fun createEventStrategyScope(
        eventHandler: EventHandler<Inputs, Events, State>,
    ): EventStrategyScope<Inputs, Events, State> = with(impl) {
        return EventStrategyScopeImpl(
            logger = logger,
            eventActor = eventActor,
            handler = eventHandler,
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun createInputHandlerScope(
        guardian: InputStrategy.Guardian,
    ): InternalInputHandlerScope<Inputs, Events, State> = with(impl) {
        return InputHandlerScopeImpl(
            guardian = guardian,
            logger = logger,
            stateActor = stateActor,
            eventActor = eventActor,
            sideJobActor = sideJobActor,
        )
    }

    override fun createInputStrategyScope(
        inputStrategyCoroutineScope: CoroutineScope,
    ): InputStrategyScope<Inputs, Events, State> = with(impl) {
        return InputStrategyScopeImpl(
            inputStrategyCoroutineScope = inputStrategyCoroutineScope,
            logger = logger,
            hostViewModelName = impl.name,
            hostViewModelType = impl.type,
            inputActor = inputActor,
            stateActor = stateActor,
            interceptorActor = interceptorActor,
        )
    }

    override fun createSideJobScope(
        sideJobCoroutineScope: CoroutineScope,
        key: String,
        restartState: SideJobScope.RestartState,
    ): SideJobScope<Inputs, Events, State> = with(impl) {
        return SideJobScopeImpl(
            sideJobCoroutineScope = sideJobCoroutineScope,
            logger = logger,
            inputActor = inputActor,
            eventActor = eventActor,
            interceptorActor = interceptorActor,
            key = key,
            restartState = restartState,
            shutDownGracePeriod = shutDownGracePeriod,
        )
    }

    override fun createStateActor(
        impl: BallastViewModelImpl<Inputs, Events, State>
    ): StateActor<Inputs, Events, State> {
        return StateActorImpl(impl)
    }
}
