package com.copperleaf.ballast.core

import com.copperleaf.ballast.EventStrategyScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

public class BufferedEventStrategy<Inputs : Any, Events : Any, State : Any> private constructor(
) : ChannelEventStrategy<Inputs, Events, State>(
    capacity = Channel.BUFFERED,
    onBufferOverflow = BufferOverflow.SUSPEND,
) {
    public override suspend fun EventStrategyScope<Inputs, Events, State>.processEvents(
        eventsQueue: Flow<Events>,
    ) {
        eventsQueue
            .collect {
                dispatchEvent(it)
            }
    }

    public companion object {
        public operator fun invoke(): BufferedEventStrategy<Any, Any, Any> {
            return BufferedEventStrategy()
        }

        public fun <Inputs : Any, Events : Any, State : Any> typed(): BufferedEventStrategy<Inputs, Events, State> {
            return BufferedEventStrategy()
        }
    }
}
