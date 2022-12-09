package com.copperleaf.ballast.examples.ui.kitchensink

object KitchenSinkContract {
    data class State(
        val inputStrategy: InputStrategySelection,
        val loading: Boolean = false,
        val completedInputCounter: Int = 0,
        val infiniteCounter: Int = 0,
    )

    sealed class Inputs {
        object GoBack : Inputs()
        object CloseKitchenSinkWindow : Inputs()
        data class ChangeInputStrategy(val inputStrategy: InputStrategySelection) : Inputs()

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
        object GoBack : Events()
        object CloseWindow : Events()
        data class NavigateTo(val directions: String) : Events()

        class LongRunningEvent : Events()
        class ErrorRunningEvent : Events()
    }
}
