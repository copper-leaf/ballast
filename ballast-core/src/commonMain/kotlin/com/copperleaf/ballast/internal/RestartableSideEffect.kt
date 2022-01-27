package com.copperleaf.ballast.internal

import com.copperleaf.ballast.SideEffectScope

internal class RestartableSideEffect<Inputs : Any, Events : Any, State : Any>(
    val key: String?,
    val block: suspend SideEffectScope<Inputs, Events, State>.() -> Unit,
)
