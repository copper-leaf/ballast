package com.copperleaf.ballast.savedstate

import com.copperleaf.ballast.BallastLogger

public interface RestoreStateScope<Inputs : Any, Events : Any, State : Any> {

    public val logger: BallastLogger
    public val hostViewModelName: String
}
