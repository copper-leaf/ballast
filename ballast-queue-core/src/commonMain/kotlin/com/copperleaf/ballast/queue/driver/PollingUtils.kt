package com.copperleaf.ballast.queue.driver

import com.copperleaf.ballast.queue.SerializedJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

public inline fun <JobMetadata : Any> pollingFlow(
    crossinline pollNext: suspend () -> SerializedJob<JobMetadata>?,
    crossinline awaitNext: suspend (emptyPollCount: Int) -> Unit,
): Flow<SerializedJob<JobMetadata>> = flow {
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
