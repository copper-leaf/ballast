package com.copperleaf.ballast.internal

import com.copperleaf.ballast.SideJobScope

internal class SideJobRequest<Inputs : Any, Events : Any, State : Any>(
    val key: String,
    val block: suspend SideJobScope<Inputs, Events, State>.() -> Unit,
)
