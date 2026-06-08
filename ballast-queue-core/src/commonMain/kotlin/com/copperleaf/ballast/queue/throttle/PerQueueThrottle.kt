package com.copperleaf.ballast.queue.throttle

import com.copperleaf.ballast.queue.QueueThrottle
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

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

    override suspend fun awaitShutdown(): Unit = coroutineScope {
        // Deduplicate by identity in case the same instance appears in both the map and as the default.
        (policies.values + default).toSet().forEach { policy ->
            launch { policy.awaitShutdown() }
        }
    }
}
