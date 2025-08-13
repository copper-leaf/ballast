package com.copperleaf.ballast.internal.scopes

import com.copperleaf.ballast.InputHandlerScope

internal interface InternalInputHandlerScope<Inputs : Any, Events : Any, State : Any> :
    InputHandlerScope<Inputs, Events, State> {

    fun markAsCompletedSuccessfully()
}
