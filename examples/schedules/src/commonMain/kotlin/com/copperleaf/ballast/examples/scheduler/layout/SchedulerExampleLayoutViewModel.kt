package com.copperleaf.ballast.examples.scheduler.layout

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.examples.scheduler.debugging
import com.copperleaf.ballast.examples.scheduler.logging
import com.copperleaf.ballast.withViewModel
import kotlinx.coroutines.CoroutineScope

class SchedulerExampleLayoutViewModel(
    coroutineScope: CoroutineScope,
) : BasicViewModel<
        SchedulerExampleLayoutContract.Inputs,
        SchedulerExampleLayoutContract.Events,
        SchedulerExampleLayoutContract.State>(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .logging()
        .debugging()
        .withViewModel(
            initialState = SchedulerExampleLayoutContract.State(),
            inputHandler = SchedulerExampleLayoutInputHandler(),
            name = "SchedulerExampleLayout"
        )
        .apply {
            inputStrategy = FifoInputStrategy.typed()
        }
        .build(),
    eventHandler = eventHandler { },
)
