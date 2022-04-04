package com.copperleaf.ballast.savedstate

import com.copperleaf.ballast.BallastLogger

public class RestoreStateScopeImpl<Inputs : Any, Events : Any, State : Any>(
    override val logger: BallastLogger,
    override val hostViewModelName: String,
) : RestoreStateScope<Inputs, Events, State>
