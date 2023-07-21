package com.copperleaf.ballast.internal

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventStrategyScope

internal class EventStrategyScopeImpl<Inputs : Any, Events : Any, State : Any>(
    private val impl: BallastViewModelImpl<Inputs, Events, State>,
    private val handler: EventHandler<Inputs, Events, State>
) : EventStrategyScope<Inputs, Events, State> {

    override val logger: BallastLogger
        get() = impl.logger

    override suspend fun dispatchEvent(event: Events) {
        impl.safelyHandleEvent(event, handler)
    }
}
