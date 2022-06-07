package com.copperleaf.ballast.examples.mainlist

import com.copperleaf.ballast.navigation.routing.Route

object MainContract {
    data class State(
        val loading: Boolean = false,
    )

    sealed class Inputs {
        object GoToCounter : Inputs()
        object GoToBoardGameGeek : Inputs()
        object GoToScorekeeper : Inputs()
        object GoToKitchenSink : Inputs()
    }

    sealed class Events {
        data class NavigateTo(val route: Route) : Events()
    }
}
