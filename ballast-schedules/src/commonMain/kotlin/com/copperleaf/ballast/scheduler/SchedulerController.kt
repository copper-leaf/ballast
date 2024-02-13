package com.copperleaf.ballast.scheduler

import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.ExperimentalBallastApi
import com.copperleaf.ballast.SideJobScope
import com.copperleaf.ballast.scheduler.executor.CoroutineClockScheduleExecutor
import com.copperleaf.ballast.scheduler.executor.CoroutineScheduleExecutor
import com.copperleaf.ballast.scheduler.vm.SchedulerContract
import com.copperleaf.ballast.scheduler.vm.SchedulerFifoInputStrategy
import com.copperleaf.ballast.scheduler.vm.SchedulerInputHandler
import com.copperleaf.ballast.withViewModel
import kotlinx.datetime.Clock

public typealias SchedulerController<I, E, S> = BallastViewModel<
        SchedulerContract.Inputs<I, E, S>,
        SchedulerContract.Events<I, E, S>,
        SchedulerContract.State<I, E, S>>

@ExperimentalBallastApi
public fun <I : Any, E : Any, S : Any> BallastViewModelConfiguration.Builder.withSchedulerController(
    clock: Clock = Clock.System,
    scheduleExecutor: CoroutineScheduleExecutor = CoroutineClockScheduleExecutor(clock),
): BallastViewModelConfiguration.TypedBuilder<
        SchedulerContract.Inputs<I, E, S>,
        SchedulerContract.Events<I, E, S>,
        SchedulerContract.State<I, E, S>> {
    return this
        .withViewModel(
            initialState = SchedulerContract.State<I, E, S>(),
            inputHandler = SchedulerInputHandler<I, E, S>(clock, scheduleExecutor),
            name = "SchedulerController",
        )
        .apply {
            this.inputStrategy = SchedulerFifoInputStrategy.typed()
        }
}

@Suppress("UNCHECKED_CAST")
public suspend fun <I : Any, E : Any, S : Any> SideJobScope<I, E, S>.scheduler(): SchedulerController<I, E, S> {
    return getInterceptor(SchedulerInterceptor.Key)
        .controller as SchedulerController<I, E, S>
}
