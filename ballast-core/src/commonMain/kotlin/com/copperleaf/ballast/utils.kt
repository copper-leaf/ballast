package com.copperleaf.ballast

import com.copperleaf.ballast.BallastNotification.InputAccepted
import com.copperleaf.ballast.BallastNotification.InputHandledSuccessfully
import com.copperleaf.ballast.BallastNotification.InputQueued
import com.copperleaf.ballast.core.DefaultViewModelConfiguration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

/**
 * Observe a Flow of Inputs that will run asynchronously in its own [SideJobScope]. This will
 * allow other Inputs to be processed and not be blocked by this Flow's subscription.
 *
 * The Flow subscription will remain active during the whole life of the associated ViewModel
 * and will be cancelled when the ViewModel is destroyed, when the side-job is restarted, or
 * it may terminate itself before then when the Flow completes.
 *
 * The side-job started from the Flow uses the resulting Input's simple class name as
 * its key.
 */
@ExperimentalCoroutinesApi
@Suppress("NOTHING_TO_INLINE")
public inline fun <
    reified Inputs : Any,
    Events : Any,
    State : Any> InputHandlerScope<Inputs, Events, State>.observeFlows(
    key: String,
    vararg inputs: Flow<Inputs>,
) {
    sideJob(
        key = key,
    ) {
        merge(*inputs)
            .onEach { postInput(it) }
            .launchIn(this)
    }
}

/**
 * Observe a Flow of Inputs that will run asynchronously in its own [SideJobScope]. This will
 * allow other Inputs to be processed and not be blocked by this Flow's subscription.
 *
 * The Flow subscription will remain active during the whole life of the associated ViewModel
 * and will be cancelled when the ViewModel is destroyed, when the side-job is restarted, or
 * it may terminate itself before then when the Flow completes.
 *
 * The side-job started from the Flow uses the resulting Input's simple class name as
 * its key.
 */
@ExperimentalCoroutinesApi
@Suppress("NOTHING_TO_INLINE")
public inline fun <
    reified Inputs : Any,
    Events : Any,
    State : Any> InputHandlerScope<Inputs, Events, State>.observeFlows(
    key: String,
    crossinline getInputs: SideJobScope<Inputs, Events, State>.() -> List<Flow<Inputs>>,
) {
    sideJob(
        key = key,
    ) {
        getInputs().merge()
            .onEach { postInput(it) }
            .launchIn(this)
    }
}

/**
 * Posts an Input back to the VM to be processed later. The Input is posted from within a side-job
 * to avoid unwanted cancellation or potential deadlocks. The current InputHandler will be completed
 * before this Input is actually dispatched to the ViewModel Input queue.
 *
 * The side-job launched here uses the `.toString()` value of [input] as the key, to avoid accidentally cancelling
 * any already-running side-jobs.
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun <Inputs : Any, Events : Any, State : Any> InputHandlerScope<Inputs, Events, State>.postInput(
    input: Inputs
) {
    sideJob(key = input.toString()) {
        this@sideJob.postInput(input)
    }
}

/**
 * Post an event which will eventually be dispatched to the VM's event handler. This event may reference values from
 * both the given Input and the current State. If the State is not needed, use [InputHandlerScope.postEvent] instead.
 */
@Suppress("NOTHING_TO_INLINE")
public suspend inline fun <
    Inputs : Any,
    Events : Any,
    State : Any> InputHandlerScope<Inputs, Events, State>.postEventWithState(
    block: (State) -> Events
) {
    val currentState = getCurrentState()
    val event = block(currentState)
    postEvent(event)
}

// Configuration DSL
// ---------------------------------------------------------------------------------------------------------------------

/**
 * Due to bug in the Kotlin language, `fun interfaces` cannot have their SAM be marked with `suspend`. This DSL method
 * is a workaround to allow that same behavior of an anonymous function as the input handler.
 */
public inline fun <Inputs : Any, Events : Any, State : Any> inputHandler(
    crossinline block: suspend InputHandlerScope<Inputs, Events, State>.(Inputs) -> Unit
): InputHandler<Inputs, Events, State> {
    return object : InputHandler<Inputs, Events, State> {
        override suspend fun InputHandlerScope<Inputs, Events, State>.handleInput(input: Inputs) {
            block(input)
        }
    }
}

/**
 * Due to bug in the Kotlin language, `fun interfaces` cannot have their SAM be marked with `suspend`. This DSL method
 * is a workaround to allow that same behavior of an anonymous function as the event handler.
 */
public inline fun <Inputs : Any, Events : Any, State : Any> eventHandler(
    crossinline block: suspend EventHandlerScope<Inputs, Events, State>.(Events) -> Unit
): EventHandler<Inputs, Events, State> {
    return object : EventHandler<Inputs, Events, State> {
        override suspend fun EventHandlerScope<Inputs, Events, State>.handleEvent(event: Events) {
            block(event)
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T : Any> Any?.requireTyped(name: String): T {
    if (this == null) error("$name required")
    return this as T
}

@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
private fun <T : Any> Any?.requireTypedIfPresent(name: String): T? {
    if (this == null) return null
    return this as T
}

@Suppress("UNCHECKED_CAST")
private fun <Inputs : Any, Events : Any, State : Any> List<BallastInterceptor<*, *, *>>.mapAsTyped(
): List<BallastInterceptor<Inputs, Events, State>> {
    return this.map { it as BallastInterceptor<Inputs, Events, State> }
}

/**
 * Create a default [BallastViewModelConfiguration] from a [BallastViewModelConfiguration.Builder].
 */
public fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.Builder.build(
): BallastViewModelConfiguration<Inputs, Events, State> {
    val vmName = name ?: "$inputHandler-vm"
    return DefaultViewModelConfiguration<Inputs, Events, State>(
        initialState = initialState.requireTyped("initialState"),
        inputHandler = inputHandler.requireTyped("inputHandler"),
        filter = filter.requireTypedIfPresent("filter"),
        interceptors = interceptors.mapAsTyped(),
        inputStrategy = inputStrategy,
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
public fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.Builder.forViewModel(
    initialState: State,
    inputHandler: InputHandler<Inputs, Events, State>,
    filter: InputFilter<Inputs, Events, State>? = null,
    name: String? = this.name,
): BallastViewModelConfiguration<Inputs, Events, State> =
    this
        .apply {
            this.initialState = initialState
            this.inputHandler = inputHandler
            this.filter = filter
            this.name = name
        }
        .build()

/**
 * Set the required properties of the Builder in a type-safe way, making sure the relevant features are all
 * type-compatible with each other even though the builder itself is untyped. Returns a fully-built
 * [BallastViewModelConfiguration].
 */
public fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.Builder.forViewModel(
    initialState: State,
    inputHandler: FilteredInputHandler<Inputs, Events, State>,
    name: String? = this.name,
): BallastViewModelConfiguration<Inputs, Events, State> =
    this
        .apply {
            this.initialState = initialState
            this.inputHandler = inputHandler
            this.filter = inputHandler
            this.name = name
        }
        .build()


/**
 * Used for keeping track of the state of discrete "subjects" within an Interceptor. For example, a single Input will
 * send Notifications for [InputQueued], [InputAccepted], and [InputHandledSuccessfully] during it's full processing
 * journey, but the `input` property of all 3 events will be the same instance, and should be associated to the same
 * value.
 *
 * This method will associate values for each Notification's subject (the Input, Event, or SideJob key) and store them
 * in the corresponding [cache] map. When [removeValue], values will be removed from the cache when they are completed.
 * Otherwise, you can add a default value to remain in the cache so that it can be read later to give a discrete signal
 * that it has finished processing.
 */
public fun <Inputs : Any, Events : Any, State : Any, T : Any> BallastNotification<Inputs, Events, State>.associate(
    cache: MutableMap<Any, T>,
    computeValueForSubject: (Any) -> T,
    onValueRemoved: (Any, T) -> Unit = { _, _ -> },

    removeValue: Boolean = true,
): T {
    val addValueToCache: (Any) -> T = {
        cache.getOrPut(it) { computeValueForSubject(it) }
    }
    val removeValueFromCache: (Any) -> T = { subject ->
        if (removeValue) {
            cache.remove(subject)
                ?.also { deferred -> onValueRemoved(subject, deferred) }
                ?: computeValueForSubject(subject).also { deferred -> onValueRemoved(subject, deferred) }
        } else {
            cache.getOrPut(subject) {
                computeValueForSubject(subject)
                    .also { deferred -> onValueRemoved(subject, deferred) }
            }
        }
    }

    return when (this) {
        is InputQueued -> {
            addValueToCache(this.input)
        }
        is InputAccepted -> {
            addValueToCache(this.input)
        }
        is BallastNotification.InputRejected -> {
            removeValueFromCache(this.input)
        }
        is BallastNotification.InputDropped -> {
            removeValueFromCache(this.input)
        }
        is InputHandledSuccessfully -> {
            removeValueFromCache(this.input)
        }
        is BallastNotification.InputCancelled -> {
            removeValueFromCache(this.input)
        }
        is BallastNotification.InputHandlerError -> {
            removeValueFromCache(this.input)
        }

        is BallastNotification.EventQueued -> {
            addValueToCache(this.event)
        }
        is BallastNotification.EventEmitted -> {
            addValueToCache(this.event)
        }
        is BallastNotification.EventHandledSuccessfully -> {
            removeValueFromCache(this.event)
        }
        is BallastNotification.EventHandlerError -> {
            removeValueFromCache(this.event)
        }

        is BallastNotification.SideJobQueued -> {
            addValueToCache(this.key)
        }
        is BallastNotification.SideJobStarted -> {
            addValueToCache(this.key)
        }
        is BallastNotification.SideJobCompleted -> {
            removeValueFromCache(this.key)
        }
        is BallastNotification.SideJobCancelled -> {
            removeValueFromCache(this.key)
        }
        is BallastNotification.SideJobError -> {
            removeValueFromCache(this.key)
        }

        is BallastNotification.StateChanged -> {
            // since states are always emitted only once and do not have status changed over time, we compute the
            // value directly and never cache it
            computeValueForSubject(this.state)
        }

        is BallastNotification.EventProcessingStarted -> {
            addValueToCache(this.vm)
        }
        is BallastNotification.EventProcessingStopped -> {
            addValueToCache(this.vm)
        }
        is BallastNotification.UnhandledError -> {
            addValueToCache(this.vm)
        }

        is BallastNotification.ViewModelStarted -> {
            addValueToCache(this.vm)
        }
        is BallastNotification.ViewModelCleared -> {
            removeValueFromCache(this.vm)
        }
    }
}
