package com.copperleaf.ballast.examples.scheduler.persistent

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.BootstrapInterceptor
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.examples.scheduler.Notifications
import com.copperleaf.ballast.examples.scheduler.debugging
import com.copperleaf.ballast.examples.scheduler.executor
import com.copperleaf.ballast.examples.scheduler.logging
import com.copperleaf.ballast.withViewModel
import kotlinx.coroutines.CoroutineScope

class PersistentSchedulesViewModel(
    coroutineScope: CoroutineScope,
) : BasicViewModel<
        PersistentSchedulesContract.Inputs,
        PersistentSchedulesContract.Events,
        PersistentSchedulesContract.State>(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .logging()
        .debugging()
        .withViewModel(
            initialState = PersistentSchedulesContract.State(),
            inputHandler = PersistentSchedulesInputHandler(executor!!, Notifications()),
            name = "PersistentSchedules"
        )
        .apply {
            inputStrategy = FifoInputStrategy.typed()
            interceptors += BootstrapInterceptor {
                PersistentSchedulesContract.Inputs.Initialize
            }
        }
        .build(),
    eventHandler = eventHandler { },
)
