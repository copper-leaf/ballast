package com.copperleaf.ballast.examples.presentation.queue

import com.copperleaf.ballast.queue.QueueDriver
import com.copperleaf.ballast.queue.driver.db.ExposedDatabaseQueueDriver
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class MainQueueAdapter(
    private val clock: Clock = Clock.System,
) : QueueDriver.Adapter<
        ExposedDatabaseQueueDriver.Metadata,
        MainQueueContract.Inputs,
        MainQueueContract.Events,
        MainQueueContract.State> {

    override fun getJobTimeout(payload: MainQueueContract.Inputs): Duration {
        return when (payload) {
            is MainQueueContract.Inputs.MainJob -> {
                payload.timeout
            }
        }
    }

    override fun getDefaultRetryDelayTimeout(payload: MainQueueContract.Inputs, attempts: Int): Duration {
        return when (payload) {
            is MainQueueContract.Inputs.MainJob -> {
                payload.retryDelay
            }
        }
    }

    override fun getJobMetadata(payload: MainQueueContract.Inputs): ExposedDatabaseQueueDriver.Metadata {
        val now = clock.now()

        return when (payload) {
            is MainQueueContract.Inputs.MainJob -> {
                ExposedDatabaseQueueDriver.Metadata(
                    insertedAt = now,
                    maxAttempts = payload.maxAttempts,
                    deduplicationKey = payload.deduplicationKey,
                    deduplicationDuration = payload.deduplicationDuration,
                )
            }
        }
    }
}
