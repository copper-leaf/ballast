package com.copperleaf.ballast.queue.executor

import kotlin.time.Duration

internal data class RunningJob<JobMetadata : Any, Payload : Any, Result : Any, State : Any>(
    val jobId: String,
    val payload: Payload,
    val state: State,
    val metadata: JobMetadata,
    val timeoutDuration: Duration,
)
