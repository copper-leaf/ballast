package com.copperleaf.ballast.test.internal.vm

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal class TestEventHandler<Inputs : Any, Events : Any, State : Any>(
    private val eventHandlerDelegate: EventHandler<Inputs, Events, State>
) : EventHandler<TestViewModel.Inputs<Inputs>, Events, State> {

    override suspend fun EventHandlerScope<TestViewModel.Inputs<Inputs>, Events, State>.handleEvent(
        event: Events
    ) {
        with(eventHandlerDelegate) {
            TestEventHandlerScope(this@handleEvent).handleEvent(event)
        }
    }
}
