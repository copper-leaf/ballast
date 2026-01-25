package com.copperleaf.ballast.queue.throttle

import com.copperleaf.ballast.queue.QueueThrottle

/**
 * A default [QueueThrottle] implementation that imposes no throttling at all, issuing permits immediately upon request
 * and maintaining no internal state.
 */
public class UnlimitedThrottle : QueueThrottle {

    override suspend fun acquirePermit(queueName: String): QueueThrottle.Permit {
        return QueueThrottle.Permit { }
    }
}
