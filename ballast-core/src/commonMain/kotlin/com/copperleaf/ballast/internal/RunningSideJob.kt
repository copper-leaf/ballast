package com.copperleaf.ballast.internal

import com.copperleaf.ballast.SideJobScope
import kotlinx.coroutines.Job

internal class RunningSideJob<Inputs : Any, Events : Any, State : Any>(
    internal val key: String,
    internal val block: suspend SideJobScope<Inputs, Events, State>.() -> Unit,
) {
    internal var job: Job? = null
}
