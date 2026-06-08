package com.copperleaf.ballast.queue

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

public inline fun <T : Any> pollingFlow(
    crossinline pollNext: suspend () -> T?,
    crossinline awaitNext: suspend (emptyPollCount: Int) -> Unit,
): Flow<T> = flow {
    var emptyPollCount = 0
    while (true) {
        val next = pollNext()

        if (next != null) {
            emit(next)
            emptyPollCount = 0
        } else {
            emptyPollCount++
            awaitNext(emptyPollCount)
        }
    }
}

public inline fun <T : Any> queueDriverPollingFlow(
    queueName: String,
    throttle: QueueThrottle,
    crossinline pollNext: suspend () -> T?,
    crossinline awaitNext: suspend (emptyPollCount: Int) -> Unit,
): Flow<T> = flow {
    var emptyPollCount = 0
    while (true) {
        // suspends until a permit is available, ensuring this worker doesn't poll jobs too quickly
        val permit = throttle.acquirePermit(queueName)

        // check the queue to see if a new job is available and ready for processing
        val next = pollNext()

        if (next != null) {
            // emit the job downstream for processing, suspending until processing is complete.
            // The permit is released in a finally block to guarantee it is always released even
            // if the collecting coroutine is cancelled while the job is in flight.
            try {
                emit(next)
            } finally {
                permit.release()
            }
            emptyPollCount = 0
        } else {
            // release the permit, allowing the throttle to issue another permit
            permit.release()

            // with no job available and no pending permit, delay the worker polling to avoid busy-looping
            emptyPollCount++
            awaitNext(emptyPollCount)
        }
    }
}
