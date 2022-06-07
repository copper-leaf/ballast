package com.copperleaf.ballast.examples.counter

object CounterContract {
    data class State(
        val count: Int = 0
    )

    sealed class Inputs {
        data class Increment(val amount: Int) : Inputs()
        data class Decrement(val amount: Int) : Inputs()
        object GoBack : Inputs()
    }

    sealed class Events {
        object NavigateBackwards : Events()
    }
}
