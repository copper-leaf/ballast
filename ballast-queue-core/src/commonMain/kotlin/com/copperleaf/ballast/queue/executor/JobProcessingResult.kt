package com.copperleaf.ballast.queue.executor

import com.copperleaf.ballast.queue.JobCompletionResult
import kotlin.time.Duration

public data class JobProcessingResult<Result : Any>(
    val jobId: String,
    val processingTime: Duration,
    val result: JobCompletionResult<Result>,
)
