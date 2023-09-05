package com.copperleaf.ballast.core

import com.copperleaf.ballast.InputFilter
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.InputStrategyScope
import com.copperleaf.ballast.Queued
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

public abstract class ChannelInputStrategy<Inputs : Any, Events : Any, State : Any>(
    capacity: Int = Channel.BUFFERED,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
    public val filter: InputFilter<Inputs, Events, State>?
) : InputStrategy<Inputs, Events, State> {
    private val _mainQueue = Channel<Queued<Inputs, Events, State>>(capacity, onBufferOverflow)
    private val _mainQueueDrained = CompletableDeferred<Unit>()

    final override fun InputStrategyScope<Inputs, Events, State>.start() {
        launch {
            _mainQueue
                .receiveAsFlow()
                .filter { queued -> filterQueued(queued) }
                .onCompletion { _mainQueueDrained.complete(Unit) }
                .let { processInputs(it) }
        }
    }

    final override suspend fun enqueue(queued: Queued<Inputs, Events, State>) {
        _mainQueue.send(queued)
    }

    final override fun tryEnqueue(queued: Queued<Inputs, Events, State>): ChannelResult<Unit> {
        return _mainQueue.trySend(queued)
    }

    final override fun close() {
        _mainQueue.close()
    }

    final override suspend fun flush() {
        _mainQueueDrained.await()
    }

    private suspend fun InputStrategyScope<Inputs, Events, State>.filterQueued(queued: Queued<Inputs, Events, State>): Boolean {
        when (queued) {
            is Queued.RestoreState -> {
                // when restoring state, always accept the item
                return true
            }

            is Queued.HandleInput -> {
                // when handling an Input, check with the InputFilter to see if it should be accepted
                val currentState = getCurrentState()
                val shouldAcceptInput = filter?.filterInput(currentState, queued.input) ?: InputFilter.Result.Accept

                if (shouldAcceptInput == InputFilter.Result.Reject) {
                    rejectInput(queued.input, currentState)
                    queued.deferred?.complete(Unit)
                }

                return shouldAcceptInput == InputFilter.Result.Accept
            }

            is Queued.ShutDownGracefully -> {
                return true
            }
        }
    }

    public abstract suspend fun InputStrategyScope<Inputs, Events, State>.processInputs(
        filteredQueue: Flow<Queued<Inputs, Events, State>>,
    )
}
