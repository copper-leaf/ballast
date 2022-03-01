package com.copperleaf.ballast.core

import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.Queued
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

/**
 * A sequential last-in-first-out strategy for processing inputs, suitable for processing UI events, as inputs will
 * cancel running work so that new inputs are handled immediately. LIFO does its best to never block the queue so that
 * UIs will always remain responsive, but it may allow for work to be cancelled unintentionally if inputs are being sent
 * from a variety of sources. Use a LIFO strategy when you care more that inputs are handled quickly, and are willing to
 * accept that work may be cancelled or some inputs dropped.
 *
 * LIFO is the default strategy if one is not explicitly provided to the ViewModel.
 *
 *
 * New inputs will be scheduled to run immediately, and any previous inputs that are still running will be cancelled.
 * LIFO guarantees that only one input will be processed at a time, and is thus protected against race
 * conditions. Each Input processed with a LIFO strategy can freely access/update the ViewModel state as many times as
 * it needs.
 *
 * Since we know only 1 Input is being procced at a time, if an input gets cancelled partway through its processing, the
 * ViewModel state will roll back to prevent the ViewModel from being left in a bad state.
 */
public class LifoInputStrategy : InputStrategy {

    override fun <T> createQueue(): Channel<T> {
        return Channel(Channel.BUFFERED, BufferOverflow.DROP_LATEST)
    }

    override val rollbackOnCancellation: Boolean = true

    override suspend fun <Inputs : Any, Events : Any, State : Any> processInputs(
        filteredQueue: Flow<Queued<Inputs, Events, State>>,
        acceptQueued: suspend (queued: Queued<Inputs, Events, State>, guardian: InputStrategy.Guardian) -> Unit,
    ) {
        filteredQueue
            .collectLatest { queued ->
                acceptQueued(queued, Guardian())
            }
    }

    public class Guardian : InputStrategy.Guardian
}
