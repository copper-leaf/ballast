package com.copperleaf.ballast.examples.presentation.ui

import androidx.compose.material3.SnackbarHostState
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope

class MainScreenEventHandler(
    private val snackbarHostState: SnackbarHostState
) : EventHandler<
        MainScreenContract.Inputs,
        MainScreenContract.Events,
        MainScreenContract.State> {
    override suspend fun EventHandlerScope<
            MainScreenContract.Inputs,
            MainScreenContract.Events,
            MainScreenContract.State>.handleEvent(
        event: MainScreenContract.Events
    ): Unit = when (event) {
        is MainScreenContract.Events.SnackbarMessage -> {
            snackbarHostState.showSnackbar(event.message)
            Unit
        }
    }
}
