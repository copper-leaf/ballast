package com.copperleaf.ballast.repository

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.FifoInputStrategy

public fun BallastViewModelConfiguration.Builder.withRepository(
): BallastViewModelConfiguration.Builder =
    this
        .apply { inputStrategy = FifoInputStrategy() }
