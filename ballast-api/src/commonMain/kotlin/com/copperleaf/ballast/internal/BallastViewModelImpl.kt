package com.copperleaf.ballast.internal

import com.copperleaf.ballast.BallastScopeFactory
import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.internal.actors.ActorCoordinator
import com.copperleaf.ballast.internal.actors.EventActor
import com.copperleaf.ballast.internal.actors.InputActor
import com.copperleaf.ballast.internal.actors.InterceptorActor
import com.copperleaf.ballast.internal.actors.SideJobActor
import com.copperleaf.ballast.internal.actors.StateActor
import com.copperleaf.ballast.internal.scopes.DefaultBallastScopeFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.flow.StateFlow

public class BallastViewModelImpl<Inputs : Any, Events : Any, State : Any>(
    internal val type: String,
    config: BallastViewModelConfiguration<Inputs, Events, State>,
) : BallastViewModel<Inputs, Events, State>,
    BallastViewModelConfiguration<Inputs, Events, State> by config {

// Internal properties
// ---------------------------------------------------------------------------------------------------------------------

    internal val scopeFactory: BallastScopeFactory<Inputs, Events, State> = DefaultBallastScopeFactory(this)
    public val inputActor: InputActor<Inputs, Events, State> = InputActor(this, scopeFactory)
    public val eventActor: EventActor<Inputs, Events, State> = EventActor(this, scopeFactory)
    public val stateActor: StateActor<Inputs, Events, State> = scopeFactory.createStateActor(this)
    public val sideJobActor: SideJobActor<Inputs, Events, State> = SideJobActor(this, scopeFactory)
    public val interceptorActor: InterceptorActor<Inputs, Events, State> = InterceptorActor(this, scopeFactory)

    internal val coordinator = ActorCoordinator(this)

    public lateinit var viewModelScope: CoroutineScope

// Core MVI pattern API
// ---------------------------------------------------------------------------------------------------------------------

    public fun start(coroutineScope: CoroutineScope) {
        coordinator.start(coroutineScope)
    }

    override fun observeStates(): StateFlow<State> {
        return stateActor.observeStates()
    }

    override suspend fun send(element: Inputs) {
        inputActor.enqueueQueued(Queued.HandleInput(null, element), await = false)
    }

    override suspend fun sendAndAwaitCompletion(element: Inputs) {
        inputActor.enqueueQueued(Queued.HandleInput(CompletableDeferred(), element), await = true)
    }

    override fun trySend(element: Inputs): ChannelResult<Unit> {
        return inputActor.enqueueQueuedImmediate(Queued.HandleInput(null, element))
    }

// ViewModel Lifecycle
// ---------------------------------------------------------------------------------------------------------------------

    public fun attachEventHandler(
        handler: EventHandler<Inputs, Events, State>,
        coroutineScope: CoroutineScope = viewModelScope,
    ) {
        eventActor.attachEventHandler(handler, coroutineScope)
    }
}
