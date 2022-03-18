package counter

object CounterContract {
    data class State(
        val count: Int = 0
    )

    sealed class Inputs {
        object Initialize : Inputs()
        object Increment : Inputs()
        object Decrement : Inputs()
    }

    sealed class Events {

    }
}
