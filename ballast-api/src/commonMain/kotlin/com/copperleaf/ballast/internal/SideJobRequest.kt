package com.copperleaf.ballast.internal

import com.copperleaf.ballast.SideJobScope

internal sealed class SideJobRequest<Inputs : Any, Events : Any, State : Any> {

    internal class StartOrRestartSideJob<Inputs : Any, Events : Any, State : Any>(
        val key: String,
        val block: suspend SideJobScope<Inputs, Events, State>.() -> Unit,
    ) : SideJobRequest<Inputs, Events, State>()

    internal class CancelSideJob<Inputs : Any, Events : Any, State : Any>(
        val key: String,
    ) : SideJobRequest<Inputs, Events, State>()
}
