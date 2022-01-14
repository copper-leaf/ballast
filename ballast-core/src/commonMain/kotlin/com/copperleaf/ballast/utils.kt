package com.copperleaf.ballast

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

/**
 * Observe a Flow of Inputs that will run asynchronously in its own [SideEffectScope]. This will
 * allow other Inputs to be processed and not be blocked by this Flow's subscription.
 *
 * The Flow subscription will remain active during the whole life of the associated ViewModel
 * and will be cancelled when the ViewModel is destroyed, when the side-effect is restarted, or
 * it may terminate itself before then when the Flow completes.
 *
 * The side-effect started from the Flow uses the resulting Input's simple class name as
 * its key.
 */
@ExperimentalCoroutinesApi
@Suppress("NOTHING_TO_INLINE")
public inline fun <
    reified Inputs : Any,
    Events : Any,
    State : Any> InputHandlerScope<Inputs, Events, State>.observeFlows(
    vararg inputs: Flow<Inputs>,
    key: String? = Inputs::class.simpleName,
    noinline onRestarted: suspend () -> Unit = { },
) {
    sideEffect(
        key = key,
        onRestarted = onRestarted,
    ) {
        coroutineScope {
            merge(*inputs)
                .onEach { postInput(it) }
                .launchIn(this)
        }
    }
}

/**
 * Posts an Input back to the VM to be processed later. The Input is posted from within a Side-Effect
 * to avoid unwanted cancellation or potential deadlocks. The current InputHandler will be completed
 * before this Input is actually dispatched to the ViewModel Input queue.
 *
 * The Side-Effect launched here uses the `.toString()` value of [input] as the key, to avoid accidentally cancelling
 * any already-running Side-Effects.
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun <Inputs : Any, Events : Any, State : Any> InputHandlerScope<Inputs, Events, State>.postInput(
    input: Inputs
) {
    sideEffect(key = input.toString()) {
        this@sideEffect.postInput(input)
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
