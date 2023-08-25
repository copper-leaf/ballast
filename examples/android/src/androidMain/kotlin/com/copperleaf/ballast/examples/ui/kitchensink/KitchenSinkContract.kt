package com.copperleaf.ballast.examples.ui.kitchensink

object KitchenSinkContract {
    data class State(
        val inputStrategy: InputStrategySelection,
        val loading: Boolean = false,
        val completedInputCounter: Int = 0,
        val infiniteCounter: Int = 0,
        val infiniteSideJobRunning: Boolean = false,
    )

    sealed interface Inputs {
        data object CloseKitchenSinkWindow : Inputs
        data class ChangeInputStrategy(val inputStrategy: InputStrategySelection) : Inputs

        data object LongRunningInput : Inputs
        data object LongRunningEvent : Inputs
        data object LongRunningSideJob : Inputs

        data object InfiniteSideJob : Inputs
        data object CancelInfiniteSideJob : Inputs
        data class IncrementInfiniteCounter(val delta: Int) : Inputs

        data object ErrorRunningInput : Inputs
        data object ErrorRunningEvent : Inputs
        data object ErrorRunningSideJob : Inputs

        data object ShutDownGracefully : Inputs
    }

    sealed interface Events {
        data object CloseWindow : Events
        data class NavigateTo(val directions: String) : Events

        data object LongRunningEvent : Events
        data object ErrorRunningEvent : Events
    }
}
