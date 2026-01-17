package com.copperleaf.ballast.queue.executor

import kotlin.time.Duration

public class JobFailureException(
    cause: Exception?,
    public val retryDelay: Duration,
) : Exception(cause)
