package com.copperleaf.ballast.examples.scheduler

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.plusAssign

internal fun BallastViewModelConfiguration.Builder.logging(): BallastViewModelConfiguration.Builder = apply {
    logger = ::platformLogger
    this += LoggingInterceptor()
}

internal fun BallastViewModelConfiguration.Builder.debugging(): BallastViewModelConfiguration.Builder {
    return installDebugger()
}
