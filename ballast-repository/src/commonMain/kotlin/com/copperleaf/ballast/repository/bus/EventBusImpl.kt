package com.copperleaf.ballast.repository.bus

import com.copperleaf.ballast.EventHandlerScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

public class EventBusImpl : EventBus {
    private val _events = MutableSharedFlow<Any>()
    override val events: SharedFlow<Any> get() = _events.asSharedFlow()

    override suspend fun <Inputs : Any, State : Any> EventHandlerScope<Inputs, Any, State>.send(event: Any) {
        _events.emit(event)
    }
}
