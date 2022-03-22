package com.copperleaf.ballast

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

public fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.Builder.build(
): BallastViewModelConfiguration<Inputs, Events, State> {
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
        name = name ?: "$inputHandler-vm",
        logger = logger,
    )
}

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

public operator fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.Builder.plusAssign(
    interceptor: BallastInterceptor<Inputs, Events, State>
) {
    this.interceptors += interceptor
}

public operator fun <Inputs : Any, Events : Any, State : Any> BallastViewModelConfiguration.Builder.plusAssign(
    interceptors: Iterable<BallastInterceptor<Inputs, Events, State>>
) {
    this.interceptors += interceptors
}

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
