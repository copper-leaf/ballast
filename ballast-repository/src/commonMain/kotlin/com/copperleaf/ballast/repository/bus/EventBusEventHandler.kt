package com.copperleaf.ballast.repository.bus

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope

/**
 * Dispatch all Events from this Repository to the shared EventBus, to be received from other Repositories as
 * appropriate.
 */
public class EventBusEventHandler<Inputs : Any, State : Any>(
    private val eventBus: EventBus
) : EventHandler<Inputs, Any, State> {
    override suspend fun EventHandlerScope<Inputs, Any, State>.handleEvent(
        event: Any
    ) {
        with(eventBus) {
            send(event)
        }
    }
}
