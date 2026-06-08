package com.copperleaf.ballast.queue

public interface QueueThrottle {

    public suspend fun acquirePermit(queueName: String): Permit

    /**
     * Signal that the queue is shutting down gracefully. Implementations must:
     * 1. Stop issuing new permits — future [acquirePermit] callers should suspend indefinitely (they will be
     *    cancelled when the queue scope is eventually torn down).
     * 2. Suspend until all currently active permits have been released, i.e. every in-flight job has finished
     *    processing.
     *
     * After this method returns it is safe to proceed with the Ballast shutdown, as no jobs are actively running.
     *
     * The default implementation is a no-op, which is appropriate for throttles that do not need to track active
     * workers (e.g. pass-through wrappers that delegate entirely to inner throttles).
     */
    public suspend fun awaitShutdown() {}

    public fun interface Permit {
        public suspend fun release()
    }
}
