package com.copperleaf.ballast.examples.ui.counter

object CounterContract {
    data class State(
        val count: Int = 0
    )

    sealed class Inputs {
        data object GoBack : Inputs()
        data class Increment(val amount: Int) : Inputs()
        data class Decrement(val amount: Int) : Inputs()
    }

    sealed class Events {
        data object GoBack : Events()
    }
}
