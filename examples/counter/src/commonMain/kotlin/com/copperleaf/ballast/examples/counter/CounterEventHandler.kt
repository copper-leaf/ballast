package com.copperleaf.ballast.examples.counter

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope

class CounterEventHandler(
    private val snackbarHostState: SnackbarHostState,
) : EventHandler<
        CounterContract.Inputs,
        CounterContract.Events,
        CounterContract.State> {
    override suspend fun EventHandlerScope<
            CounterContract.Inputs,
            CounterContract.Events,
            CounterContract.State>.handleEvent(
        event: CounterContract.Events
    ): Unit = when (event) {
        is CounterContract.Events.OnTenReached -> {
            val result = snackbarHostState.showSnackbar(
                message = "You hit ten!",
                actionLabel = "Reset",
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            )

            when (result) {
                SnackbarResult.Dismissed -> {
                    // ignore
                }

                SnackbarResult.ActionPerformed -> {
                    postInput(CounterContract.Inputs.Reset)
                }
            }
        }
    }
}
