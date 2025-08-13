package com.copperleaf.ballast

import com.copperleaf.ballast.internal.scopes.InternalEventHandlerScope
import com.copperleaf.ballast.internal.scopes.InternalInputHandlerScope
import kotlinx.coroutines.CoroutineScope

internal interface BallastScopeFactory<Inputs : Any, Events : Any, State : Any> {

    public fun createBallastInterceptorScope(
        interceptorCoroutineScope: CoroutineScope,
    ): BallastInterceptorScope<Inputs, Events, State>

    public fun createEventHandlerScope(
    ): InternalEventHandlerScope<Inputs, Events, State>

    public fun createEventStrategyScope(
        eventHandler: EventHandler<Inputs, Events, State>,
    ): EventStrategyScope<Inputs, Events, State>

    public fun createInputHandlerScope(
        guardian: InputStrategy.Guardian
    ): InternalInputHandlerScope<Inputs, Events, State>

    public fun createInputStrategyScope(
        inputStrategyCoroutineScope: CoroutineScope,
    ): InputStrategyScope<Inputs, Events, State>

    public fun createSideJobScope(
        sideJobCoroutineScope: CoroutineScope,
        key: String,
        restartState: SideJobScope.RestartState,
    ): SideJobScope<Inputs, Events, State>
}
