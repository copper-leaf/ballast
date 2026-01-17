package com.copperleaf.ballast.queue.executor

import kotlin.time.Duration

internal data class RunningJob<Payload : Any, Result : Any, State : Any>(
    val jobId: String,
    val payload: Payload,
    val state: State,
    val timeoutDuration: Duration,
)
