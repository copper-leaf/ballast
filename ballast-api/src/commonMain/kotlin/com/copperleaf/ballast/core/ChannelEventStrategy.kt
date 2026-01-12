package com.copperleaf.ballast.core

import com.copperleaf.ballast.EventStrategy
import com.copperleaf.ballast.EventStrategyScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

public abstract class ChannelEventStrategy<Inputs : Any, Events : Any, State : Any>(
    capacity: Int = Channel.BUFFERED,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
) : EventStrategy<Inputs, Events, State> {
    private val eventsQueue: Channel<QueuedEvent> = Channel(capacity, onBufferOverflow)
    private val eventsQueueDrained: CompletableDeferred<Unit> = CompletableDeferred()

    final override suspend fun EventStrategyScope<Inputs, Events, State>.start() {
        eventsQueue
            .receiveAsFlow()
            .onEach {
                when (it) {
                    is HandleEvent -> {
                        // don't process the event yet
                    }

                    is GracefullyShutDownEvents -> {
                        eventsQueueDrained.complete(Unit)
                    }
                }
            }
            .filterIsInstance<HandleEvent>()
            .map { it.event }
            .let { processEvents(it) }
    }

    final override suspend fun enqueue(event: Events) {
        eventsQueue.send(HandleEvent(event))
    }

    final override fun close() {
        val result = eventsQueue.trySend(GracefullyShutDownEvents())

        if (result.isSuccess) {
            eventsQueue.close()
        }
    }

    final override suspend fun flush() {
        eventsQueueDrained.await()
    }

    public abstract suspend fun EventStrategyScope<Inputs, Events, State>.processEvents(
        eventsQueue: Flow<Events>,
    )

    private abstract inner class QueuedEvent
    private inner class HandleEvent(val event: Events) : QueuedEvent()
    private inner class GracefullyShutDownEvents : QueuedEvent()
}
