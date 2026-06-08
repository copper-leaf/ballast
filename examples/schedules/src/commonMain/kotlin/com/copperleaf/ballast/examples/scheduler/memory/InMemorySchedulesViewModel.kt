package com.copperleaf.ballast.examples.scheduler.memory

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.examples.scheduler.debugging
import com.copperleaf.ballast.examples.scheduler.logging
import com.copperleaf.ballast.examples.scheduler.memory.schedule.InMemorySchedulesAdapter
import com.copperleaf.ballast.plusAssign
import com.copperleaf.ballast.scheduler.SchedulerInterceptor
import com.copperleaf.ballast.withViewModel
import kotlinx.coroutines.CoroutineScope

class InMemorySchedulesViewModel(
    coroutineScope: CoroutineScope,
    scheduler: SchedulerInterceptor<
            InMemorySchedulesContract.Inputs,
            InMemorySchedulesContract.Events,
            InMemorySchedulesContract.State>
) : BasicViewModel<
        InMemorySchedulesContract.Inputs,
        InMemorySchedulesContract.Events,
        InMemorySchedulesContract.State>(
    coroutineScope = coroutineScope,
    config = BallastViewModelConfiguration.Builder()
        .logging()
        .debugging()
        .apply { this += scheduler }
        .withViewModel(
            initialState = InMemorySchedulesContract.State(),
            inputHandler = InMemorySchedulesInputHandler(),
            name = "InMemorySchedules"
        )
        .apply {
            inputStrategy = FifoInputStrategy.typed()
        }
        .build(),
    eventHandler = eventHandler { },
)

internal fun createScheduler(): SchedulerInterceptor<
        InMemorySchedulesContract.Inputs,
        InMemorySchedulesContract.Events,
        InMemorySchedulesContract.State> {
    return SchedulerInterceptor(
        extraConfig = {
            it.logging().debugging()
        },
        initialSchedule = InMemorySchedulesAdapter(),
    )
}
