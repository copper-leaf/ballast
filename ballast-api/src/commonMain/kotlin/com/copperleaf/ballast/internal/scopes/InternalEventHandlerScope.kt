package com.copperleaf.ballast.internal.scopes

import com.copperleaf.ballast.EventHandlerScope

internal interface InternalEventHandlerScope<Inputs : Any, Events : Any, State : Any> :
    EventHandlerScope<Inputs, Events, State>,
    AutoCloseable
