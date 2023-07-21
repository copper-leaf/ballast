package com.copperleaf.ballast

public interface EventStrategy<Inputs : Any, Events : Any, State : Any> {
    public suspend fun EventStrategyScope<Inputs, Events, State>.start()

    public suspend fun enqueue(event: Events)

    /**
     * Immediately mark the InputStrategy as closed. After this function returns, no more Inputs may be sent to the VM,
     * though anything currently in the queue may still be processed.
     */
    public fun close()

    /**
     * Suspend until everything in the queue has been fully processed.
     */
    public suspend fun flush()
}
