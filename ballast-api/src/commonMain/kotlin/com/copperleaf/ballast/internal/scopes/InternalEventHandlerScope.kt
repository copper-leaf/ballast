package com.copperleaf.ballast.internal.scopes

import com.copperleaf.ballast.EventHandlerScope

public interface InternalEventHandlerScope<Inputs : Any, Events : Any, State : Any> :
    EventHandlerScope<Inputs, Events, State> {

    public fun markAsCompletedSuccessfully()
}
