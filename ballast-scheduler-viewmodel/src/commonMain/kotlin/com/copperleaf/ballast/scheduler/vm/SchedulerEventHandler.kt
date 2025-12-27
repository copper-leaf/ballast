package com.copperleaf.ballast.scheduler.vm

import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope

internal class SchedulerEventHandler<I : Any, E : Any, S : Any>(
    private val interceptorScope: BallastInterceptorScope<I, E, S>
) : EventHandler<
        SchedulerContract.Inputs<I, E, S>,
        SchedulerContract.Events<I, E, S>,
        SchedulerContract.State<I, E, S>> {
    override suspend fun EventHandlerScope<
            SchedulerContract.Inputs<I, E, S>,
            SchedulerContract.Events<I, E, S>,
            SchedulerContract.State<I, E, S>>.handleEvent(
        event: SchedulerContract.Events<I, E, S>
    ): Unit = when (event) {
        is SchedulerContract.Events.PostInputToHost -> {
            interceptorScope.sendToQueue(event.queued)
        }
    }
}
