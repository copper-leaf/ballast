package com.copperleaf.ballast

import com.copperleaf.ballast.core.DefaultViewModelConfiguration
import kotlinx.coroutines.CoroutineDispatcher

// Public APIs for typed Builder
// ---------------------------------------------------------------------------------------------------------------------

/**
 * Create a default [BallastViewModelConfiguration] from a [BallastViewModelConfiguration.Builder].
 */
public fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.TypedBuilder<Inputs, Events, State>.build(
): BallastViewModelConfiguration<Inputs, Events, State> {
    val vmName = name ?: "$inputHandler-vm"
    @Suppress("DEPRECATION")
    return DefaultViewModelConfiguration<Inputs, Events, State>(
        initialState = initialState.requireTyped("initialState"),
        inputHandler = inputHandler.requireTyped("inputHandler"),
        interceptors = interceptors.mapAsTyped(),
        inputStrategy = inputStrategy.requireTyped("inputStrategy"),
        eventStrategy = eventStrategy.requireTyped("eventStrategy"),
        inputsDispatcher = inputsDispatcher,
        eventsDispatcher = eventsDispatcher,
        sideJobsDispatcher = sideJobsDispatcher,
        interceptorDispatcher = interceptorDispatcher,
        name = vmName,
        logger = logger(vmName),
    )
}

/**
 * Set all [CoroutineDispatcher]s in your ViewModel. For convenience, you can set only the [inputsDispatcher] to use
 * that for all dispatchers, or set the dispatcher for each feature individually.
 */
public fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.TypedBuilder<Inputs, Events, State>.dispatchers(
    inputsDispatcher: CoroutineDispatcher,
    eventsDispatcher: CoroutineDispatcher = inputsDispatcher,
    sideJobsDispatcher: CoroutineDispatcher = inputsDispatcher,
    interceptorDispatcher: CoroutineDispatcher = inputsDispatcher,
): BallastViewModelConfiguration.TypedBuilder<Inputs, Events, State> = apply {
    this.inputsDispatcher = inputsDispatcher
    this.eventsDispatcher = eventsDispatcher
    this.sideJobsDispatcher = sideJobsDispatcher
    this.interceptorDispatcher = interceptorDispatcher
}

/**
 * Add a [BallastInterceptor] to the [BallastViewModelConfiguration.Builder].
 */
public operator fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.TypedBuilder<Inputs, Events, State>.plusAssign(
    interceptor: BallastInterceptor<Inputs, Events, State>
) {
    this.interceptors += interceptor
}

/**
 * Add many [BallastInterceptor]s to the [BallastViewModelConfiguration.Builder].
 */
public operator fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.TypedBuilder<Inputs, Events, State>.plusAssign(
    interceptors: Iterable<BallastInterceptor<Inputs, Events, State>>
) {
    this.interceptors += interceptors
}
