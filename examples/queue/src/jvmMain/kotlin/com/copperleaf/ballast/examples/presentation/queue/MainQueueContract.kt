package com.copperleaf.ballast.examples.presentation.queue

import com.copperleaf.ballast.examples.presentation.models.QueueName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

object MainQueueContract {
    @Serializable
    data class State(
        val step: Int = 0,
    )

    @Serializable
    sealed interface Inputs {
        @Serializable
        data class MainJob(
            val queue: QueueName,
            val timeout: Duration,
            val retryDelay: Duration,
            val maxAttempts: Int,
            val successAttemptIndex: Int,
            val processingTime: Duration,
            val deduplicationKey: String?,
            val deduplicationDuration: Duration,
            val messageGroup: String?,
            val resultValue: String?,
        ) : Inputs
    }

    @Serializable
    sealed interface Events {
        @Serializable
        data class JobCompleted(
            val resultValue: String,
        ) : Events
    }
}
