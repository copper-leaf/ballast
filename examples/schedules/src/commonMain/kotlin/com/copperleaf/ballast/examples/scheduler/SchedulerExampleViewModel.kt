package com.copperleaf.ballast.examples.scheduler

import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.build
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.FifoInputStrategy
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.debugger.BallastDebuggerInterceptor
import com.copperleaf.ballast.plusAssign
import com.copperleaf.ballast.scheduler.SchedulerInterceptor
import com.copperleaf.ballast.scheduler.withSchedulerController
import com.copperleaf.ballast.withViewModel
import kotlinx.coroutines.CoroutineScope

typealias SchedulerExampleViewModel = BallastViewModel<
        SchedulerExampleContract.Inputs,
        SchedulerExampleContract.Events,
        SchedulerExampleContract.State>

// Build VM
// ---------------------------------------------------------------------------------------------------------------------

internal fun createScheduler(): SchedulerInterceptor<
        SchedulerExampleContract.Inputs,
        SchedulerExampleContract.Events,
        SchedulerExampleContract.State> {
    return SchedulerInterceptor(
        config = BallastViewModelConfiguration.Builder()
            .logging()
            .debugging()
            .withSchedulerController<
                    SchedulerExampleContract.Inputs,
                    SchedulerExampleContract.Events,
                    SchedulerExampleContract.State>()
            .build(),
        initialSchedule = SchedulerExampleAdapter(),
    )
}
internal fun createViewModel(
    viewModelCoroutineScope: CoroutineScope,
    scheduler: SchedulerInterceptor<
            SchedulerExampleContract.Inputs,
            SchedulerExampleContract.Events,
            SchedulerExampleContract.State>
): SchedulerExampleViewModel {
    return BasicViewModel(
        coroutineScope = viewModelCoroutineScope,
        config = BallastViewModelConfiguration.Builder()
            .logging()
            .debugging()
            .apply { this += scheduler }
            .withViewModel(
                initialState = SchedulerExampleContract.State(),
                inputHandler = SchedulerExampleInputHandler(),
                name = "SchedulerExample"
            )
            .apply {
                inputStrategy = FifoInputStrategy.typed()
            }
            .build(),
        eventHandler = SchedulerExampleEventHandler(),
    )
}

private fun BallastViewModelConfiguration.Builder.logging(): BallastViewModelConfiguration.Builder = apply {
    logger = ::platformLogger
    this += LoggingInterceptor()
}

private fun BallastViewModelConfiguration.Builder.debugging(): BallastViewModelConfiguration.Builder = apply {
    this += BallastDebuggerInterceptor(platformDebuggerConnection())
}
