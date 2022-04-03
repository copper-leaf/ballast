package com.copperleaf.ballast.savedstate

public interface PerformRestoreStateScope<Inputs : Any, Events : Any, State : Any> {

    public val hostViewModelName: String
}
