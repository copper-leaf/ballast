package com.copperleaf.ballast.examples.presentation.ui

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.examples.presentation.queue.MainQueueContract
import com.copperleaf.ballast.examples.presentation.queue.MainQueueViewModel
import com.copperleaf.ballast.observeFlows
import com.copperleaf.ballast.queue.driver.db.repository.JobsMaintenanceRepository
import com.copperleaf.ballast.queue.driver.db.repository.JobsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

class MainScreenInputHandler(
    val jobsRepository: JobsRepository,
    val jobsMaintenanceRepository: JobsMaintenanceRepository,
    val queueViewModel: MainQueueViewModel,
) : InputHandler<
        MainScreenContract.Inputs,
        MainScreenContract.Events,
        MainScreenContract.State> {

    override suspend fun InputHandlerScope<
            MainScreenContract.Inputs,
            MainScreenContract.Events,
            MainScreenContract.State>.handleInput(
        input: MainScreenContract.Inputs
    ): Unit = when (input) {
        is MainScreenContract.Inputs.Initialize -> {
            val jobsFlow = flow {
                while (true) {
                    emit(jobsRepository.getAllJobs().sortedByDescending { it.metadata.insertedAt })
                    delay(1.seconds)
                }
            }.conflate()
                .distinctUntilChanged()
                .map { MainScreenContract.Inputs.JobsUpdated(it) }

            observeFlows("Initialize", jobsFlow)
        }

        is MainScreenContract.Inputs.JobsUpdated -> {
            updateState { it.copy(jobs = input.jobs) }
        }

        is MainScreenContract.Inputs.DeleteOldJobs -> {
            sideJob("DeleteOldJobs") {
                jobsMaintenanceRepository.deleteOldJobs(duration = 1.seconds)
                postEvent(MainScreenContract.Events.SnackbarMessage("Old jobs deleted"))
            }
        }

        is MainScreenContract.Inputs.FreeJobCooldowns -> {
            sideJob("FreeJobCooldowns") {
                jobsMaintenanceRepository.freeJobCooldowns()
                postEvent(MainScreenContract.Events.SnackbarMessage("Cooldowns freed"))
            }
        }

        is MainScreenContract.Inputs.RetryHungJobs -> {
            sideJob("RetryHungJobs") {
                jobsMaintenanceRepository.retryHungJobs()
                postEvent(MainScreenContract.Events.SnackbarMessage("Hung jobs freed"))
            }
        }

        is MainScreenContract.Inputs.EnqueueNewJob -> {
            sideJob("EnqueueNewJob") {
                queueViewModel.send(
                    MainQueueContract.Inputs.MainJob(
                        queue = input.queueName,
                        timeout = input.timeoutSeconds.seconds,
                        retryDelay = input.retryDelaySeconds.seconds,
                        maxAttempts = input.maxAttempts,
                        successAttemptIndex = input.successAttemptIndex,
                        processingTime = input.processingTimeSeconds.seconds,
                        deduplicationKey = input.deduplicationKey.takeIf { it.isNotBlank() },
                        deduplicationDuration = input.deduplicationDuration.seconds,
                        resultValue = input.resultValue.takeIf { it.isNotBlank() },
                    )
                )
            }
        }

        is MainScreenContract.Inputs.ToggleAllRowSelection -> {
            updateState {
                it.copy(
                    selectedJobs = if (input.selected) {
                        it.jobs.map { state -> state.jobId }.toSet()
                    } else {
                        emptySet()
                    }
                )
            }
        }
        is MainScreenContract.Inputs.ToggleRowSelection -> {
            updateState {
                it.copy(
                    selectedJobs = if (input.jobId in it.selectedJobs) {
                        it.selectedJobs - input.jobId
                    } else {
                        it.selectedJobs + input.jobId
                    }
                )
            }
        }

        is MainScreenContract.Inputs.ViewJobDetails -> {
            updateState { it.copy(selectedJobId = input.jobId) }
        }

        is MainScreenContract.Inputs.CancelJob -> {
            singleOrBulkJobOperation(
                inputJobId = input.jobId,
                successMessage = "Cancellation requested",
                operation = { jobId -> jobsRepository.requestCancellation(jobId) }
            )
        }

        is MainScreenContract.Inputs.DeleteJob -> {
            singleOrBulkJobOperation(
                inputJobId = input.jobId,
                successMessage = "Deletion requested",
                operation = { jobId -> jobsRepository.deleteJob(jobId) }
            )
        }

        is MainScreenContract.Inputs.ForceRetry -> {
            singleOrBulkJobOperation(
                inputJobId = input.jobId,
                successMessage = "Force retry requested",
                operation = { jobId -> jobsRepository.forceRetry(jobId) }
            )
        }
    }

    private suspend fun InputHandlerScope<
            MainScreenContract.Inputs,
            MainScreenContract.Events,
            MainScreenContract.State>.singleOrBulkJobOperation(
        inputJobId: String?,
        successMessage: String,
        operation: suspend (Uuid) -> Unit
    ) {
        val currentState = getCurrentState()

        if (inputJobId != null) {
            operation(Uuid.parse(inputJobId))
            postEvent(MainScreenContract.Events.SnackbarMessage("$successMessage for job $inputJobId"))
        } else {
            currentState.selectedJobs.forEach { jobId ->
                operation(Uuid.parse(jobId))
            }
            postEvent(MainScreenContract.Events.SnackbarMessage("$successMessage for ${currentState.selectedJobs.size} jobs"))
        }

        updateState {
            it.copy(
                selectedJobs = emptySet(),
                selectedJobId = if (it.selectedJobId == inputJobId) {
                    null
                } else {
                    it.selectedJobId
                }
            )
        }
    }
}
