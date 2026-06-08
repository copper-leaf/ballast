package com.copperleaf.ballast.examples.scheduler.memory

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.examples.scheduler.memory.schedule.InMemorySchedulesAdapter
import com.copperleaf.ballast.scheduler.scheduler
import com.copperleaf.ballast.scheduler.vm.SchedulerContract
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class InMemorySchedulesInputHandler(
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.UTC,
) : InputHandler<
        InMemorySchedulesContract.Inputs,
        InMemorySchedulesContract.Events,
        InMemorySchedulesContract.State> {
    override suspend fun InputHandlerScope<
            InMemorySchedulesContract.Inputs,
            InMemorySchedulesContract.Events,
            InMemorySchedulesContract.State>.handleInput(
        input: InMemorySchedulesContract.Inputs
    ) = when (input) {
        is InMemorySchedulesContract.Inputs.Increment -> {
            updateState {
                it.copy(
                    count = it.count + input.amount,
                    scheduledUpdateTimes = it.scheduledUpdateTimes.toMutableList()
                        .apply {
                            this += (input.scheduleKey to clock.now().toLocalDateTime(timeZone))
                        }
                        .toList()
                )
            }

            delay(input.processingTime)
        }

        is InMemorySchedulesContract.Inputs.StartSchedules -> {
            updateState {
                it.copy(
                    scheduledUpdateTimes = emptyList()
                )
            }

            sideJob("Start schedules") {
                scheduler()
                    .send(
                        SchedulerContract.Inputs.StartSchedules(
                            InMemorySchedulesAdapter()
                        )
                    )
            }
        }

        is InMemorySchedulesContract.Inputs.PauseSchedule -> {
            sideJob("Pause ${input.key}") {
                scheduler()
                    .send(SchedulerContract.Inputs.PauseSchedule(input.key))
            }
        }

        is InMemorySchedulesContract.Inputs.ResumeSchedule -> {
            sideJob("Resume ${input.key}") {
                scheduler()
                    .send(SchedulerContract.Inputs.ResumeSchedule(input.key))
            }
        }

        is InMemorySchedulesContract.Inputs.StopSchedule -> {
            updateState {
                it.copy(
                    scheduledUpdateTimes = it
                        .scheduledUpdateTimes
                        .filter { it.first != input.key }
                )
            }
            sideJob("Stop ${input.key}") {
                scheduler()
                    .send(SchedulerContract.Inputs.CancelSchedule(input.key))
            }
        }
    }
}
