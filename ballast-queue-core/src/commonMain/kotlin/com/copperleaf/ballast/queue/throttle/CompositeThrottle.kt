package com.copperleaf.ballast.queue.throttle

import com.copperleaf.ballast.queue.QueueThrottle
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * A [QueueThrottle] implementation that requires a worker to satisfy all the provided throttle [policies] before
 * issuing its own permit to the worker. When this permit is released, all the underlying permits acquired from each
 * policy are also released.
 *
 * This throttle will wait for each underlying policy in parallel using the [async]/[awaitAll] pattern to acquire
 * each individual permit. The total wait time is not additive; it will be the greatest wait time of any individual
 * policy.
 */
public class CompositeThrottle(
    private vararg val policies: QueueThrottle
) : QueueThrottle {

    override suspend fun acquirePermit(queueName: String): QueueThrottle.Permit = coroutineScope {
        val permits = policies
            .map { async { it.acquirePermit(queueName) } }
            .awaitAll()

        QueueThrottle.Permit {
            permits.forEach { it.release() }
        }
    }

    override suspend fun awaitShutdown(): Unit = coroutineScope {
        policies.forEach { policy -> launch { policy.awaitShutdown() } }
    }
}
