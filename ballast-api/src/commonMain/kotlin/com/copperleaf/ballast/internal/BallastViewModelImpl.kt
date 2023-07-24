package com.copperleaf.ballast.internal

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

    internal val inputActor = InputActor(this)
    internal val eventActor = EventActor(this)
    internal val stateActor = StateActor(this)
    internal val sideJobActor = SideJobActor(this)
    internal val interceptorActor = InterceptorActor(this)

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
