package com.copperleaf.ballast.core

import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.InputStrategyScope
import com.copperleaf.ballast.Queued
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

/**
 * A sequential first-in-first-out strategy for processing inputs, suitable for background processing. As inputs will be
 * queued instead of running immediately, it is not suitable for processing UI inputs, as the queue could easily be
 * suspended and leave the UI unresponsive. Use a FIFO strategy when you care more that inputs are not dropped, than
 * that they get processed quickly.
 *
 * New inputs will be queued such that the first inputs received will run to completion before later ones start
 * processing. FIFO guarantees that only one input will be processed at a time, and is thus protected against race
 * conditions. Each Input processed with a FIFO strategy can freely access/update the ViewModel state as many times as
 * it needs. FIFO also guarantees that inputs will not be cancelled unless the entire ViewModel gets cancelled.
 *
 * Since we know only 1 Input is being procced at a time, if an input gets cancelled partway through its processing, the
 * ViewModel state will roll back to prevent the ViewModel from being left in a bad state.
 */
public class FifoInputStrategy<Inputs : Any, Events : Any, State : Any> private constructor(): InputStrategy<Inputs, Events, State> {

    override fun createQueue(): Channel<Queued<Inputs, Events, State>> {
        return Channel(Channel.BUFFERED, BufferOverflow.SUSPEND)
    }

    override val rollbackOnCancellation: Boolean = true

    override suspend fun InputStrategyScope<Inputs, Events, State>.processInputs(
        filteredQueue: Flow<Queued<Inputs, Events, State>>,
    ) {
        filteredQueue
            .collect { queued ->
                acceptQueued(queued, DefaultGuardian())
            }
    }

    public companion object {
        public operator fun invoke() : FifoInputStrategy<Any, Any, Any> {
            return FifoInputStrategy()
        }

        public fun <Inputs : Any, Events : Any, State : Any> typed(): FifoInputStrategy<Inputs, Events, State> {
            return FifoInputStrategy()
        }
    }
}
