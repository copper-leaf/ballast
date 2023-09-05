package com.copperleaf.ballast.internal.scopes

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventStrategyScope
import com.copperleaf.ballast.internal.actors.EventActor

internal class EventStrategyScopeImpl<Inputs : Any, Events : Any, State : Any>(
    override val logger: BallastLogger,

    private val eventActor: EventActor<Inputs, Events, State>,

    private val handler: EventHandler<Inputs, Events, State>,
) : EventStrategyScope<Inputs, Events, State> {

    override suspend fun dispatchEvent(event: Events) {
        eventActor.safelyHandleEvent(event, handler)
    }
}
