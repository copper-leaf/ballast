package com.copperleaf.ballast.savedstate

public class PerformRestoreStateScopeImpl<Inputs : Any, Events : Any, State : Any>(
    override val hostViewModelName: String
) : PerformRestoreStateScope<Inputs, Events, State>
