package com.copperleaf.ballast

import com.copperleaf.ballast.core.DefaultViewModelConfiguration
import kotlinx.coroutines.CoroutineDispatcher

// Public APIs for untyped Builder
// ---------------------------------------------------------------------------------------------------------------------

/**
 * Create a default [BallastViewModelConfiguration] from a [BallastViewModelConfiguration.Builder].
 */
public fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.Builder.build(
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
 * Create a default [BallastViewModelConfiguration] from a [BallastViewModelConfiguration.Builder].
 */
public fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.Builder.typedBuilder(
): BallastViewModelConfiguration.TypedBuilder<Inputs, Events, State> {
    val vmName = name ?: "$inputHandler-vm"
    return BallastViewModelConfiguration.TypedBuilder<Inputs, Events, State>(
        initialState = initialState.requireTypedIfPresent("initialState"),
        inputHandler = inputHandler.requireTypedIfPresent("inputHandler"),
        interceptors = interceptors.mapAsTyped<Inputs, Events, State>().toMutableList(),
        inputStrategy = inputStrategy.requireTypedInputStrategyIfPresent(),
        eventStrategy = eventStrategy.requireTypedEventStrategyIfPresent(),
        inputsDispatcher = inputsDispatcher,
        eventsDispatcher = eventsDispatcher,
        sideJobsDispatcher = sideJobsDispatcher,
        interceptorDispatcher = interceptorDispatcher,
        name = vmName,
        logger = logger,
    )
}

/**
 * Set all [CoroutineDispatcher]s in your ViewModel. For convenience, you can set only the [inputsDispatcher] to use
 * that for all dispatchers, or set the dispatcher for each feature individually.
 */
public fun BallastViewModelConfiguration.Builder.dispatchers(
    inputsDispatcher: CoroutineDispatcher,
    eventsDispatcher: CoroutineDispatcher = inputsDispatcher,
    sideJobsDispatcher: CoroutineDispatcher = inputsDispatcher,
    interceptorDispatcher: CoroutineDispatcher = inputsDispatcher,
): BallastViewModelConfiguration.Builder = apply {
    this.inputsDispatcher = inputsDispatcher
    this.eventsDispatcher = eventsDispatcher
    this.sideJobsDispatcher = sideJobsDispatcher
    this.interceptorDispatcher = interceptorDispatcher
}

/**
 * Add a [BallastInterceptor] to the [BallastViewModelConfiguration.Builder].
 */
public operator fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.Builder.plusAssign(
    interceptor: BallastInterceptor<Inputs, Events, State>
) {
    this.interceptors += interceptor
}

/**
 * Add many [BallastInterceptor]s to the [BallastViewModelConfiguration.Builder].
 */
public operator fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.Builder.plusAssign(
    interceptors: Iterable<BallastInterceptor<Inputs, Events, State>>
) {
    this.interceptors += interceptors
}

/**
 * Set the required properties of the Builder in a type-safe way, making sure the relevant features are all
 * type-compatible with each other even though the builder itself is untyped. Returns a fully-built
 * [BallastViewModelConfiguration].
 */
public fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.Builder.withViewModel(
    initialState: State,
    inputHandler: InputHandler<Inputs, Events, State>,
    name: String? = this.name,
): BallastViewModelConfiguration.TypedBuilder<Inputs, Events, State> =
    this
        .typedBuilder<Inputs, Events, State>()
        .apply {
            this.initialState = initialState
            this.inputHandler = inputHandler
            this.name = name
        }

/**
 * Set the required properties of the Builder in a type-safe way, making sure the relevant features are all
 * type-compatible with each other even though the builder itself is untyped. Returns a fully-built
 * [BallastViewModelConfiguration].
 */
@Deprecated("InputFilter is no longer used by the VM configuration. Pass the filter to the InputStrategy instead.")
@Suppress("UNUSED_PARAMETER")
public fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.Builder.withViewModel(
    initialState: State,
    inputHandler: InputHandler<Inputs, Events, State>,
    filter: InputFilter<Inputs, Events, State>?,
    name: String? = this.name,
): BallastViewModelConfiguration.TypedBuilder<Inputs, Events, State> {
    throw NotImplementedError("InputFilter is no longer used by the VM configuration. Pass the filter to the InputStrategy instead.")
}

/**
 * Set the required properties of the Builder in a type-safe way, making sure the relevant features are all
 * type-compatible with each other even though the builder itself is untyped. Returns a fully-built
 * [BallastViewModelConfiguration].
 */
@Deprecated("InputFilter is no longer used by the VM configuration. Pass the filter to the InputStrategy instead.")
@Suppress("UNUSED_PARAMETER")
public fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.Builder.withViewModel(
    initialState: State,
    inputHandler: FilteredInputHandler<Inputs, Events, State>,
    name: String? = this.name,
): BallastViewModelConfiguration.TypedBuilder<Inputs, Events, State> {
    throw NotImplementedError("InputFilter is no longer used by the VM configuration. Pass the filter to the InputStrategy instead.")
}

// Internal Helpers
// ---------------------------------------------------------------------------------------------------------------------


@Suppress("UNCHECKED_CAST")
internal fun <T : Any> Any?.requireTyped(name: String): T {
    if (this == null) error("$name required")
    return this as T
}

@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
internal fun <T : Any> Any?.requireTypedIfPresent(name: String): T? {
    if (this == null) return null
    return this as T
}

@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
internal fun <Inputs : Any, Events : Any, State : Any> InputStrategy<*, *, *>?.requireTypedInputStrategyIfPresent(): InputStrategy<Inputs, Events, State>? {
    if (this == null) return null
    return this as InputStrategy<Inputs, Events, State>
}

@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
internal fun <Inputs : Any, Events : Any, State : Any> EventStrategy<*, *, *>?.requireTypedEventStrategyIfPresent(): EventStrategy<Inputs, Events, State>? {
    if (this == null) return null
    return this as EventStrategy<Inputs, Events, State>
}

@Suppress("UNCHECKED_CAST")
internal fun <Inputs : Any, Events : Any, State : Any> List<BallastInterceptor<*, *, *>>.mapAsTyped(
): List<BallastInterceptor<Inputs, Events, State>> {
    return this.map { it as BallastInterceptor<Inputs, Events, State> }
}
