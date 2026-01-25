package com.copperleaf.ballast.queue.throttle

import com.copperleaf.ballast.queue.QueueThrottle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
        // wait for the bucket to fill up enough to take a token. The result isn't actually used; we just need to
        // wait until there's at least one token available.
        tokens.first { it > 0 }

        // once we've confirmed there's at least one token, take it out of the bucket
        tokens.update { it - 1 }

        return QueueThrottle.Permit {}
    }
}
