package com.copperleaf.ballast.queue.throttle

import com.copperleaf.ballast.queue.QueueThrottle

/**
 * A [QueueThrottle] implementation that applies different throttling [policies] based on the queue name provided
 * when acquiring a permit. If no specific policy is found for the given queue name, the [default] policy is applied.
 */
public class PerQueueThrottle(
    private val policies: Map<String, QueueThrottle>,
    private val default: QueueThrottle
) : QueueThrottle {

    override suspend fun acquirePermit(queueName: String): QueueThrottle.Permit {
        val policy = policies[queueName] ?: default
        return policy.acquirePermit(queueName)
    }
}
