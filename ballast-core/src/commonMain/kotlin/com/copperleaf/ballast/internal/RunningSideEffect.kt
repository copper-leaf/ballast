package com.copperleaf.ballast.internal

import com.copperleaf.ballast.SideEffectScope
import kotlinx.coroutines.Job

internal class RunningSideEffect<Inputs : Any, Events : Any, State : Any>(
    internal val key: String,
    internal val block: suspend SideEffectScope<Inputs, Events, State>.() -> Unit,
) {
    internal var job: Job? = null
}
