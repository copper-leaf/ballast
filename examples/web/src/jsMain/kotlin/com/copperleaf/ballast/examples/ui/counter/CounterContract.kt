package com.copperleaf.ballast.examples.ui.counter

import kotlinx.serialization.Serializable

object CounterContract {
    @Serializable
    data class State(
        val count: Int = 0
    )

    @Serializable
    sealed interface Inputs {
        @Serializable
        data class Increment(val amount: Int) : Inputs
        @Serializable
        data class Decrement(val amount: Int) : Inputs
    }

    @Serializable
    sealed interface Events {
    }
}
