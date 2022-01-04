package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope

internal class TestEventHandler<Inputs : Any, Events : Any, State : Any>(
    private val eventHandlerDelegate: EventHandler<Inputs, Events, State>
) : EventHandler<TestViewModel.Inputs<Inputs, State>, Events, State> {

    override suspend fun EventHandlerScope<TestViewModel.Inputs<Inputs, State>, Events, State>.handleEvent(
        event: Events
    ) {
        with(eventHandlerDelegate) {
            TestEventHandlerScope(this@handleEvent).handleEvent(event)
        }
    }
}
