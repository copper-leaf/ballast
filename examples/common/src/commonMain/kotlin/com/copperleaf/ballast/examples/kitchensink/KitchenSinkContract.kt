package com.copperleaf.ballast.examples.kitchensink

object KitchenSinkContract {
    data class State(
        val loading: Boolean = false,
        val infiniteCounter: Int = 0,
    )

    sealed class Inputs {
        object CloseKitchenSinkWindow : Inputs()

        class LongRunningInput : Inputs()
        object LongRunningEvent : Inputs()
        object LongRunningSideEffect : Inputs()

        object InfiniteSideEffect : Inputs()
        object CancelInfiniteSideEffect : Inputs()
        data class IncrementInfiniteCounter(val delta: Int) : Inputs()

        object ErrorRunningInput : Inputs()
        object ErrorRunningEvent : Inputs()
        object ErrorRunningSideEffect : Inputs()
    }

    sealed class Events {
        object CloseWindow : Events()

        class LongRunningEvent : Events()
        class ErrorRunningEvent : Events()
    }
}
