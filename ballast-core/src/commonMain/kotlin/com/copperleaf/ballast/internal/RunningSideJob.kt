package com.copperleaf.ballast.internal

import com.copperleaf.ballast.SideJobScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job

internal class RunningSideJob(
    internal val key: String,
    internal var restartState: SideJobScope.RestartState,
    internal var job: Job,
    internal val onCompletion: CompletableDeferred<Unit>,
)
