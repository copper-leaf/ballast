package com.copperleaf.ballast.queue

public fun interface QueueThrottle {

    public suspend fun acquirePermit(queueName: String): Permit

    public fun interface Permit {
        public suspend fun release()
    }
}
