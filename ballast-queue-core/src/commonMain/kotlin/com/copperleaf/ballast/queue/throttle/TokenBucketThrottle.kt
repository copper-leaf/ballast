package com.copperleaf.ballast.queue.throttle

import com.copperleaf.ballast.queue.QueueThrottle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.min
import kotlin.time.Duration

/**
 * A [QueueThrottle] implementation that uses the token bucket algorithm to limit the rate of work processing.
 *
 * The bucket has a maximum [capacity] of tokens. Tokens are added to the bucket at a rate of [refillRatePerTick]
 * every [tickDuration]. When a worker wants to process work, it must acquire a token from the bucket. If no tokens
 * are available, the worker will suspend until a token becomes available.
 *
 * Conceptually, you can imagine a bucket slowly filling with water from a tap (tokens). When a worker wants to process
 * work, it takes out a cup of water (a single token). If the bucket is empty, the worker must wait until enough water
 * fills the bucket to fill its cup.
 *
 * This implementation uses a [CoroutineScope] to launch a coroutine immediately upon creation that refills the bucket
 * at the specified rate.
 */
public class TokenBucketThrottle(
    scope: CoroutineScope,
    capacity: Int,
    refillRatePerTick: Int,
    tickDuration: Duration,
) : QueueThrottle {

    private val tokens = MutableStateFlow(capacity)

    private val mutex = Mutex()
    private var shuttingDown = false
    private val activePermits = MutableStateFlow(0)

    init {
        scope.launch {
            while (isActive) {
                delay(tickDuration)
                tokens.update { current ->
                    min(capacity, current + refillRatePerTick)
                }
            }
        }
    }

    override suspend fun acquirePermit(queueName: String): QueueThrottle.Permit {
        // Atomically claim an active slot before suspending on the token wait. This ensures awaitShutdown() cannot
        // return a count of zero while a worker is mid-acquisition waiting for a token.
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
        if (permit == null) awaitCancellation()

        // Wait for the bucket to fill up enough to take a token. Wrap in try/catch so that
        // if this coroutine is cancelled while waiting, the active slot claimed above is
        // released and awaitShutdown() is not left hanging.
        try {
            // Atomically wait for and consume a token using a CAS loop to avoid racing with
            // concurrent workers that may observe the same positive count.
            while (true) {
                val current = tokens.value
                if (current > 0 && tokens.compareAndSet(current, current - 1)) break
                if (current <= 0) tokens.first { it > 0 }
            }
        } catch (e: CancellationException) {
            activePermits.update { it - 1 }
            throw e
        }

        return permit
    }

    override suspend fun awaitShutdown() {
        mutex.withLock { shuttingDown = true }
        activePermits.first { it == 0 }
    }
}
