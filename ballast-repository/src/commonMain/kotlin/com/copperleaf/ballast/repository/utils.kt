package com.copperleaf.ballast.repository

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.FifoInputStrategy

/**
 * Set the required properties of the Builder in a type-safe way, making sure the relevant features are all
 * type-compatible with each other even though the builder itself is untyped. Returns a fully-built
 * [BallastViewModelConfiguration].
 */
public fun BallastViewModelConfiguration.Builder.withRepository(
): BallastViewModelConfiguration.Builder =
    this
        .apply { inputStrategy = FifoInputStrategy() }

/**
 * Set the required properties of the Builder in a type-safe way, making sure the relevant features are all
 * type-compatible with each other even though the builder itself is untyped. Returns a fully-built
 * [BallastViewModelConfiguration].
 */
public fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.TypedBuilder<Inputs, Events, State>.withRepository(
): BallastViewModelConfiguration.TypedBuilder<Inputs, Events, State> =
    this
        .apply { inputStrategy = FifoInputStrategy.typed() }
