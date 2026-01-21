package com.copperleaf.ballast.queue.driver

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
            awaitNext(emptyPollCount)
        }
    }
}
