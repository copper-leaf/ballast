package com.copperleaf.ballast.examples.scheduler

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.scheduler.vm.SchedulerContract
import kotlinx.datetime.LocalDateTime

@ExperimentalMaterial3Api
object SchedulerExampleUi {

    @Composable
    fun Content() {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val schedulerInterceptor = remember { createScheduler() }
        val vm: SchedulerExampleViewModel = remember(viewModelCoroutineScope, schedulerInterceptor) {
            createViewModel(viewModelCoroutineScope, schedulerInterceptor)
        }
        val uiState by vm.observeStates().collectAsState()
        val schedulerState by schedulerInterceptor.controller.observeStates().collectAsState()

        Content(uiState, schedulerState) { vm.trySend(it) }
    }

    @Composable
    public fun Content(
        uiState: SchedulerExampleContract.State,
        schedulerState: SchedulerContract.State<
                SchedulerExampleContract.Inputs,
                SchedulerExampleContract.Events,
                SchedulerExampleContract.State>,
        postInput: (SchedulerExampleContract.Inputs) -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${uiState.count}",
                    style = MaterialTheme.typography.headlineLarge,
                )
            }

            Button({ postInput(SchedulerExampleContract.Inputs.StartSchedules) }) {
                Text("Start Schedules")
            }

            schedulerState.schedules.values.forEach { schedule ->
                Row {
                    Column {
                        Text("Schedule: ${schedule.key}")
                        Text("started at:  ${schedule.startedAt}")
                        Text("first update at:  ${schedule.firstUpdateAt}")
                        Text("latest update at:  ${schedule.latestUpdateAt}")
                        Text("numberOfDispatchedInputs:  ${schedule.numberOfDispatchedInputs}")
                        Text("numberOfDroppedInputs:  ${schedule.numberOfDroppedInputs}")
                        Text("numberOfFailedInputs:  ${schedule.numberOfFailedInputs}")

                        Button({ postInput(SchedulerExampleContract.Inputs.StopSchedule(schedule.key)) }) {
                            Text("Cancel")
                        }

                        if (!schedule.paused) {
                            Button({ postInput(SchedulerExampleContract.Inputs.PauseSchedule(schedule.key)) }) {
                                Text("Pause")
                            }
                        } else {
                            Button({ postInput(SchedulerExampleContract.Inputs.ResumeSchedule(schedule.key)) }) {
                                Text("Resume")
                            }
                        }
                    }
                    Column(Modifier.height(180.dp).verticalScroll(rememberScrollState())) {
                        uiState.scheduledUpdateTimes.filter { it.first == schedule.key }.forEach { dateTime ->
                            Text(
                                text = "Scheduled event sent at ${dateTime.second.format()}",
                            )
                        }
                    }
                }

                Divider()
            }
        }
    }

    private fun LocalDateTime.format(): String {
        return "${formatDate()} at ${formatTime()}"
    }

    private fun LocalDateTime.formatDate(): String {
        return "${month.name} $dayOfMonth, $year"
    }

    private fun LocalDateTime.formatTime(): String {
        if (hour > 12) {
            return "${hour - 12}:$minute pm (${second}s)"
        } else {
            return "$hour:$minute am (${second}s)"
        }
    }
}
