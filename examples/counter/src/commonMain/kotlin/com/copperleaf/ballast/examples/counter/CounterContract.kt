package com.copperleaf.ballast.examples.counter

object CounterContract {
    data class State(
        val count: Int = 0
    )

    sealed interface Inputs {
        data class Increment(val amount: Int) : Inputs
        data class Decrement(val amount: Int) : Inputs
        data object Reset : Inputs
    }

    sealed interface Events {
        data object OnTenReached : Events
    }
}
