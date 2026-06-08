package com.copperleaf.ballast.queue

public interface QueueThrottle {

    public suspend fun acquirePermit(queueName: String): Permit

    /**
     * Signal that the queue is shutting down gracefully. Implementations must:
     * 1. Immediately stop issuing new permits — future [acquirePermit] callers should suspend indefinitely (they will be
     *    cancelled when the queue scope is eventually torn down).
     * 2. Suspend until all currently active permits have been released, i.e. every in-flight job has finished
     *    processing.
     *
     * After this method returns it is safe to proceed with the Ballast shutdown, as no jobs are actively running.
     */
    public suspend fun awaitShutdown() {}

    public fun interface Permit {
        public suspend fun release()
    }
}
