package com.copperleaf.ballast.examples.mainlist

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.examples.navigation.Routes

class MainInputHandler : InputHandler<
    MainContract.Inputs,
    MainContract.Events,
    MainContract.State> {
    override suspend fun InputHandlerScope<
        MainContract.Inputs,
        MainContract.Events,
        MainContract.State>.handleInput(
        input: MainContract.Inputs
    ) = when (input) {
        is MainContract.Inputs.GoToBoardGameGeek -> {
            postEvent(MainContract.Events.NavigateTo(Routes.BoardGameGeek))
        }
        is MainContract.Inputs.GoToCounter -> {
            postEvent(MainContract.Events.NavigateTo(Routes.Counter))
        }
        is MainContract.Inputs.GoToKitchenSink -> {
            postEvent(MainContract.Events.NavigateTo(Routes.KitchenSink))
        }
        is MainContract.Inputs.GoToScorekeeper -> {
            postEvent(MainContract.Events.NavigateTo(Routes.Scorekeeper))
        }
    }
}
