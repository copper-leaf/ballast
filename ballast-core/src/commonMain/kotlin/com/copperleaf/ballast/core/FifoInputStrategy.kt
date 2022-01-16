package com.copperleaf.ballast.core

import com.copperleaf.ballast.InputStrategy
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

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
public class FifoInputStrategy : InputStrategy {

    override fun <T> createChannel(): Channel<T> {
        return Channel(Channel.BUFFERED, BufferOverflow.SUSPEND)
    }

    override val rollbackOnCancellation: Boolean = true

    override suspend fun <Inputs : Any> processInputs(
        filteredInputs: Flow<Inputs>,
        acceptInput: suspend (Inputs, (InputStrategy.InputResult) -> Unit) -> Unit,
    ) {
        filteredInputs
            .collect { input ->
                acceptInput(input) { }
            }
    }
}
