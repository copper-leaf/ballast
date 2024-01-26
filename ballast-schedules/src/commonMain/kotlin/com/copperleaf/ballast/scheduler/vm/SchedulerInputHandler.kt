package com.copperleaf.ballast.scheduler.vm

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.internal.restartableJob
import com.copperleaf.ballast.scheduler.executor.CoroutineScheduleExecutor
import com.copperleaf.ballast.scheduler.internal.SchedulerAdapterScopeImpl
import kotlinx.datetime.Clock

internal class SchedulerInputHandler<I : Any, E : Any, S : Any>(
    private val clock: Clock,
    private val scheduleExecutor: CoroutineScheduleExecutor,
) : InputHandler<
        SchedulerContract.Inputs<I, E, S>,
        SchedulerContract.Events<I, E, S>,
        SchedulerContract.State<I, E, S>> {
    override suspend fun InputHandlerScope<
            SchedulerContract.Inputs<I, E, S>,
            SchedulerContract.Events<I, E, S>,
            SchedulerContract.State<I, E, S>>.handleInput(
        input: SchedulerContract.Inputs<I, E, S>
    ): Unit = when (input) {
        is SchedulerContract.Inputs.StartSchedules -> {
            // run the adapter to get the schedules which should run
            val adapterScope = SchedulerAdapterScopeImpl<I, E, S>()
            with(input.adapter) {
                adapterScope.configureSchedules()
            }

            // keep track of which schedules are active
            val now = clock.now()
            updateState {
                it.copy(
                    schedules = it.schedules
                        .toMutableMap()
                        .apply {
                            adapterScope.schedules.forEach { schedule ->
                                this[schedule.key] = ScheduleState(schedule.key, now)
                            }
                        }
                        .toMap()
                )
            }

            // cancel any running schedules which have the same keys as the newly requested schedules
            adapterScope.schedules.forEach { schedule ->
                cancelSideJob(schedule.key)
            }

            // then create the new schedules, running each in their own SideJob
            adapterScope.schedules.forEach { schedule ->
                // this would normally be blocked by the Guardian of the InputStrategy, but here we're using a custom
                // guardian which allows this operation. Notably, schedules cannot update the Scheduler state, but only read
                // it. Race conditions aren't a huge issue here, a slightly out-of-date State is fine.
                val isPaused = suspend {
                    getCurrentState().schedules[schedule.key]?.paused == true
                }

                sideJob(schedule.key) {
                    val postInputRestartableJob = this.restartableJob<SchedulerContract.Inputs<I, E, S>> { input ->
                        postInput(input)
                    }

                    // run the schedule, sending an Event with each tick. This may suspend indefinitely for infinite schedules
                    scheduleExecutor.runSchedule(
                        schedule = schedule.schedule,
                        delayMode = schedule.delayMode,
                        shouldHandleTask = { !isPaused() },
                        onTaskDropped = { // onTaskDropped
                            postInputRestartableJob.restart(
                                SchedulerContract.Inputs.ScheduledTaskDropped(schedule.key)
                            )
                        },
                        enqueueTask = { _, deferred -> // enqueueTask
                            postInputRestartableJob.restart(
                                SchedulerContract.Inputs.DispatchScheduledTask(
                                    schedule.key,
                                    Queued.HandleInput(deferred, schedule.scheduledInput())
                                )
                            )
                        },
                    )

                    // if the schedule was finite, once it finishes, send an Input to remove it from the VM state
                    postInput(SchedulerContract.Inputs.MarkScheduleComplete(schedule.key))
                }
            }
        }

        is SchedulerContract.Inputs.DispatchScheduledTask -> {
            val now = clock.now()
            updateScheduleState(input.key) {
                it.copy(
                    firstUpdateAt = it.firstUpdateAt ?: now,
                    latestUpdateAt = now,
                    numberOfDispatchedInputs = it.numberOfDispatchedInputs + 1
                )
            }

            postEvent(
                SchedulerContract.Events.PostInputToHost(input.queued)
            )
        }

        is SchedulerContract.Inputs.PauseSchedule -> {
            updateScheduleState(input.key) {
                it.copy(paused = true)
            }
        }

        is SchedulerContract.Inputs.ResumeSchedule -> {
            updateScheduleState(input.key) {
                it.copy(paused = false)
            }
        }

        is SchedulerContract.Inputs.CancelSchedule -> {
            updateScheduleState(input.key) {
                null
            }
            cancelSideJob(input.key)
        }

        is SchedulerContract.Inputs.MarkScheduleComplete -> {
            updateScheduleState(input.key) {
                null
            }
        }

        is SchedulerContract.Inputs.ScheduledTaskDropped -> {
            updateScheduleState(input.key) {
                it.copy(
                    numberOfDroppedInputs = it.numberOfDroppedInputs + 1
                )
            }
        }

        is SchedulerContract.Inputs.ScheduledTaskFailed -> {
            updateScheduleState(input.key) {
                it.copy(
                    numberOfFailedInputs = it.numberOfFailedInputs + 1
                )
            }
        }
    }

    private suspend fun InputHandlerScope<
            SchedulerContract.Inputs<I, E, S>,
            SchedulerContract.Events<I, E, S>,
            SchedulerContract.State<I, E, S>>.updateScheduleState(
        key: String,
        block: (ScheduleState) -> ScheduleState?,
    ) {
        updateState {
            it.copy(
                schedules = it.schedules
                    .toMutableMap()
                    .apply {
                        val updatedState = (this[key] ?: ScheduleState(key, clock.now())).let(block)

                        if (updatedState != null) {
                            this[key] = updatedState
                        } else {
                            this.remove(key)
                        }
                    }
                    .toMap()
            )
        }
    }
}
