package com.copperleaf.ballast.scheduler.vm

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.scheduler.ScheduleExecutor
import com.copperleaf.ballast.scheduler.internal.RegisteredSchedule
import com.copperleaf.ballast.scheduler.internal.SchedulerAdapterScopeImpl
import kotlinx.coroutines.flow.filter
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi

internal class SchedulerInputHandler<I : Any, E : Any, S : Any>(
    private val clock: Clock,
    private val scheduleExecutor: ScheduleExecutor,
) : InputHandler<
        SchedulerContract.Inputs<I, E, S>,
        SchedulerContract.Events<I, E, S>,
        SchedulerContract.State<I, E, S>> {
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun InputHandlerScope<
            SchedulerContract.Inputs<I, E, S>,
            SchedulerContract.Events<I, E, S>,
            SchedulerContract.State<I, E, S>>.handleInput(
        input: SchedulerContract.Inputs<I, E, S>
    ): Unit = when (input) {
        is SchedulerContract.Inputs.StartSchedules -> {
            val currentIndex = getAndUpdateState { it.copy(scheduleIndex = it.scheduleIndex + 1) }.scheduleIndex

            // run the adapter to get the schedules which should run
            val adapterScope = SchedulerAdapterScopeImpl<I, E, S>()
            with(input.adapter) {
                adapterScope.configureSchedules()
            }

            // add the schedule to the list of running schedules
            val now = clock.now()
            updateState {
                it.copy(
                    schedules = it.schedules
                        .toMutableMap()
                        .apply {
                            adapterScope.schedules.forEach { schedule ->
                                this[schedule.schedule.name] = ScheduleState(schedule.schedule.name, now)
                            }
                        }
                        .toMap()
                )
            }

            val isPaused: suspend (String?) -> Boolean = { scheduleName: String? ->
                getCurrentState().schedules[scheduleName]?.paused == true
            }

            sideJob("StartSchedules-$currentIndex") {
                // run the schedule, sending an Event with each tick. This may suspend indefinitely for infinite schedules
                scheduleExecutor
                    .runSchedules(adapterScope.schedules.map { it.schedule })
                    .filter { emission -> !isPaused(emission.name) }
                    .collect { emission ->
                        val registeredSchedule: RegisteredSchedule<I, E, S> = adapterScope
                            .schedules
                            .single { it.schedule.name == emission.name }
                        postInput(
                            SchedulerContract.Inputs.DispatchScheduledTask(
                                emission.name,
                                Queued.HandleInput(null, registeredSchedule.scheduledInput(emission.triggeredAt))
                            )
                        )
                    }
            }
        }

        is SchedulerContract.Inputs.StartSchedule -> {
            val currentIndex = getAndUpdateState { it.copy(scheduleIndex = it.scheduleIndex + 1) }.scheduleIndex

            // add the schedule to the list of running schedules
            val now = clock.now()
            updateState {
                it.copy(
                    schedules = it.schedules
                        .toMutableMap()
                        .apply {
                            this[input.schedule.name] = ScheduleState(input.schedule.name, now)
                        }
                        .toMap()
                )
            }

            // then create the new schedules, running each in their own SideJob
            // this would normally be blocked by the Guardian of the InputStrategy, but here we're using a custom
            // guardian which allows this operation. Notably, schedules cannot update the Scheduler state, but only read
            // it. Race conditions aren't a huge issue here, a slightly out-of-date State is fine.
            val isPaused = suspend {
                getCurrentState().schedules[input.schedule.name]?.paused == true
            }

            sideJob("StartSchedule-$currentIndex") {
                // run the schedule, sending an Event with each tick. This may suspend indefinitely for infinite schedules
                scheduleExecutor
                    .runSchedule(input.schedule)
                    .filter { !isPaused() }
                    .collect { emission ->
                        postInput(
                            SchedulerContract.Inputs.DispatchScheduledTask(
                                input.schedule.name,
                                Queued.HandleInput(null, input.scheduledInput(emission.triggeredAt))
                            )
                        )
                    }

                // if the schedule was finite, once it finishes, send an Input to remove it from the VM state
                postInput(SchedulerContract.Inputs.MarkScheduleComplete(input.schedule.name))
            }
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
    }

    private suspend fun InputHandlerScope<
            SchedulerContract.Inputs<I, E, S>,
            SchedulerContract.Events<I, E, S>,
            SchedulerContract.State<I, E, S>>.updateScheduleState(
        key: String?,
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
