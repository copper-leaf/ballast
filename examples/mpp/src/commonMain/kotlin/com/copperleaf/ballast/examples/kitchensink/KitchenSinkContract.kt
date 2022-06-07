package com.copperleaf.ballast.examples.kitchensink

object KitchenSinkContract {
    data class State(
        val loading: Boolean = false,
        val completedInputCounter: Int = 0,
        val infiniteCounter: Int = 0,
    )

    sealed class Inputs {
        object CloseKitchenSinkWindow : Inputs()

        class LongRunningInput : Inputs()
        object LongRunningEvent : Inputs()
        object LongRunningSideJob : Inputs()

        object InfiniteSideJob : Inputs()
        object CancelInfiniteSideJob : Inputs()
        data class IncrementInfiniteCounter(val delta: Int) : Inputs()

        object ErrorRunningInput : Inputs()
        object ErrorRunningEvent : Inputs()
        object ErrorRunningSideJob : Inputs()
    }

    sealed class Events {
        class LongRunningEvent : Events()
        class ErrorRunningEvent : Events()

        object NavigateBackwards : Events()
    }
}
