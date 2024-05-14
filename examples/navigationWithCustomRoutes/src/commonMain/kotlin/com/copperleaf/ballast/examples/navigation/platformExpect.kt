package com.copperleaf.ballast.examples.navigation

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastViewModelConfiguration

internal expect fun BallastViewModelConfiguration.Builder.installDebugger(): BallastViewModelConfiguration.Builder

internal expect fun platformLogger(loggerName: String): BallastLogger
