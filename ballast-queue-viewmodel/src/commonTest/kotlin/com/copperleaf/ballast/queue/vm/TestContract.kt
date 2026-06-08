package com.copperleaf.ballast.queue.vm

import kotlinx.serialization.Serializable

object TestContract {
    @Serializable
    data class State(
        val step: Int = 0,
    )

    @Serializable
    sealed interface Inputs {
        @Serializable
        data class AsyncJob(val inputData: String) : Inputs
    }

    @Serializable
    sealed interface Events {
        @Serializable
        data class JobCompleted(val result: String) : Events
    }
}
