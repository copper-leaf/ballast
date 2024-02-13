package com.copperleaf.ballast.examples.scheduler

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.scheduler.scheduler
import com.copperleaf.ballast.scheduler.vm.SchedulerContract
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class SchedulerExampleInputHandler(
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.UTC,
) : InputHandler<
        SchedulerExampleContract.Inputs,
        SchedulerExampleContract.Events,
        SchedulerExampleContract.State> {
    override suspend fun InputHandlerScope<
            SchedulerExampleContract.Inputs,
            SchedulerExampleContract.Events,
            SchedulerExampleContract.State>.handleInput(
        input: SchedulerExampleContract.Inputs
    ) = when (input) {
        is SchedulerExampleContract.Inputs.Increment -> {
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

        is SchedulerExampleContract.Inputs.StartSchedules -> {
            updateState {
                it.copy(
                    scheduledUpdateTimes = emptyList()
                )
            }

            sideJob("Start schedules") {
                scheduler()
                    .send(
                        SchedulerContract.Inputs.StartSchedules(
                            SchedulerExampleAdapter()
                        )
                    )
            }
        }

        is SchedulerExampleContract.Inputs.PauseSchedule -> {
            sideJob("Pause ${input.key}") {
                scheduler()
                    .send(SchedulerContract.Inputs.PauseSchedule(input.key))
            }
        }

        is SchedulerExampleContract.Inputs.ResumeSchedule -> {
            sideJob("Resume ${input.key}") {
                scheduler()
                    .send(SchedulerContract.Inputs.ResumeSchedule(input.key))
            }
        }

        is SchedulerExampleContract.Inputs.StopSchedule -> {
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
