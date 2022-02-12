package com.copperleaf.ballast.repository.bus

import com.copperleaf.ballast.EventHandlerScope
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.repository.BallastRepository
import kotlinx.coroutines.flow.SharedFlow

/**
 * EventBus is a mechanism to allow Ballast Repository ViewModel instances to communicate internally with each other
 * without requiring a hard dependency between the repositories. The sending Repository must call [InputHandlerScope.postEvent]
 * with an Input of the receiving repository. The receiving Repository can then react to that request by collecting
 * events of its own Input type with [observeInputsFromBus].
 */
public interface EventBus {
    /**
     * A flow of all events emitted to this Bus.
     */
    public val events: SharedFlow<Any>

    /**
     * Send an Input into the Bus, to distribute to other Repositories as needed. This should only be called internally,
     * from a [BallastRepository]'s EventHandler, and should not be called directly. Instead, Inputs should be sent to
     * the Bus with [InputHandlerScope.postEvent], when handling one of its own events.
     */
    public suspend fun <Inputs : Any, State : Any> EventHandlerScope<Inputs, Any, State>.send(event: Any)
}
