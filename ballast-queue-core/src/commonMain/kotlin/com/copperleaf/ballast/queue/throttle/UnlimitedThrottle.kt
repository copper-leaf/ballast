package com.copperleaf.ballast.queue.throttle

import com.copperleaf.ballast.queue.QueueThrottle
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A default [QueueThrottle] implementation that imposes no throttling at all, issuing permits immediately upon
 * request. An internal counter tracks how many permits are currently active so that [awaitShutdown] can wait until
 * all in-flight jobs have finished before returning.
 */
public class UnlimitedThrottle : QueueThrottle {

    private val mutex = Mutex()
    private var shuttingDown = false
    private val activePermits = MutableStateFlow(0)

    override suspend fun acquirePermit(queueName: String): QueueThrottle.Permit {
        // Atomically check the shutdown flag and claim a slot. Returns null when shutting down so we can
        // suspend outside the lock without holding it indefinitely.
        val permit = mutex.withLock {
            if (shuttingDown) {
                null
            } else {
                activePermits.update { it + 1 }
                QueueThrottle.Permit {
                    activePermits.update { it - 1 }
                }
            }
        }
        return permit ?: awaitCancellation()
    }

    override suspend fun awaitShutdown() {
        mutex.withLock { shuttingDown = true }
        activePermits.first { it == 0 }
    }
}
