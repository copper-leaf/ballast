package com.copperleaf.ballast.examples.presentation.ui

import com.copperleaf.ballast.examples.presentation.models.JobsTableCell
import com.copperleaf.ballast.examples.presentation.models.JobsTableColumn
import com.copperleaf.ballast.examples.presentation.models.QueueName
import com.copperleaf.ballast.queue.SerializedJob
import com.copperleaf.ballast.queue.driver.db.ExposedDatabaseQueueDriver

object MainScreenContract {
    data class State(
        val jobs: List<SerializedJob<ExposedDatabaseQueueDriver.Metadata>> = emptyList(),
        val tableColumns: List<JobsTableColumn> = JobsTableColumn.defaultTableColumns(),
        val detailColumns: List<JobsTableColumn> = JobsTableColumn.defaultDetailsColumns(),

        val selectedJobId: String? = null,
        val selectedJobs: Set<String> = emptySet(),
    ) {
        val selectedJob: SerializedJob<ExposedDatabaseQueueDriver.Metadata>? = jobs.find { it.jobId == selectedJobId }

        val tableCells: List<JobsTableCell> = (listOf(null) + jobs).flatMapIndexed { rowIndex, job ->
            tableColumns.mapIndexed { columnIndex, column ->
                JobsTableCell(
                    job = job,
                    column = column,
                    rowIndex = rowIndex,
                    columnIndex = columnIndex,
                )
            }
        }
    }

    sealed interface Inputs {
        data object Initialize : Inputs
        data class JobsUpdated(val jobs: List<SerializedJob<ExposedDatabaseQueueDriver.Metadata>>) : Inputs

        // queue maintenance
        data object DeleteOldJobs : Inputs
        data object FreeJobCooldowns : Inputs
        data object RetryHungJobs : Inputs

        // enqueue new jobs
        data class EnqueueNewJob(
            val queueName: QueueName,
            val timeoutSeconds: Int,
            val retryDelaySeconds: Int,
            val maxAttempts: Int,
            val successAttemptIndex: Int,
            val processingTimeSeconds: Int,
            val deduplicationKey: String,
            val deduplicationDuration: Int,
            val resultValue: String,
        ) : Inputs

        // operations on selected job, or bulk operations if jobId is null
        data class ToggleAllRowSelection(val selected: Boolean) : Inputs
        data class ToggleRowSelection(val jobId: String) : Inputs
        data class ViewJobDetails(val jobId: String?) : Inputs
        data class CancelJob(val jobId: String?) : Inputs
        data class DeleteJob(val jobId: String?) : Inputs
        data class ForceRetry(val jobId: String?) : Inputs
    }

    sealed interface Events {
        data class SnackbarMessage(val message: String) : Events
    }
}
