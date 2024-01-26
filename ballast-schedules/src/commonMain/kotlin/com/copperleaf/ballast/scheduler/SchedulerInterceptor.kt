package com.copperleaf.ballast.scheduler

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.ExperimentalBallastApi
import com.copperleaf.ballast.awaitViewModelStart
import com.copperleaf.ballast.build
import com.copperleaf.ballast.internal.BallastViewModelImpl
import com.copperleaf.ballast.scheduler.vm.SchedulerContract
import com.copperleaf.ballast.scheduler.vm.SchedulerEventHandler
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@ExperimentalBallastApi
public class SchedulerInterceptor<I : Any, E : Any, S : Any>(
    private val config: BallastViewModelConfiguration<
            SchedulerContract.Inputs<I, E, S>,
            SchedulerContract.Events<I, E, S>,
            SchedulerContract.State<I, E, S>> = BallastViewModelConfiguration.Builder()
        .withSchedulerController<I, E, S>()
        .build(),
    private val initialSchedule: SchedulerAdapter<I, E, S>? = null
) : BallastInterceptor<I, E, S> {

    public object Key : BallastInterceptor.Key<SchedulerInterceptor<*, *, *>>

    override val key: BallastInterceptor.Key<SchedulerInterceptor<*, *, *>> = SchedulerInterceptor.Key

    private val _controller = BallastViewModelImpl("SchedulerController", config)
    public val controller: SchedulerController<I, E, S> get() = _controller

    override fun BallastInterceptorScope<I, E, S>.start(
        notifications: Flow<BallastNotification<I, E, S>>,
    ) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            notifications.awaitViewModelStart()

            _controller.start(this)

            launch {
                _controller.attachEventHandler(SchedulerEventHandler(this@start))
            }

            if (initialSchedule != null) {
                _controller.send(SchedulerContract.Inputs.StartSchedules(adapter = initialSchedule))
            }
        }
    }

    override fun toString(): String {
        return "SchedulerInterceptor"
    }
}
