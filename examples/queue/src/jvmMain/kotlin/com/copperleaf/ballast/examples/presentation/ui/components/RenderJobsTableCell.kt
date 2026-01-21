package com.copperleaf.ballast.examples.presentation.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.examples.presentation.models.JobsTableCell
import com.copperleaf.ballast.examples.presentation.models.JobsTableColumn
import com.copperleaf.ballast.examples.presentation.models.QueueName
import com.copperleaf.ballast.examples.presentation.ui.MainScreenContract
import com.copperleaf.ballast.examples.presentation.utils.formatted
import com.copperleaf.ballast.queue.JobCompletionResultType
import com.copperleaf.ballast.queue.SerializedJob
import com.copperleaf.ballast.queue.driver.DatabaseJobStatus
import com.copperleaf.ballast.queue.driver.DatabaseQueueDriver
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

val JobsTableColumn.columnWidth: Dp
    get() = when (this) {
        JobsTableColumn.ViewDetailsButton -> 160.dp
        JobsTableColumn.ActionsMenu -> 80.dp
        JobsTableColumn.ToggleSelection -> 80.dp
        JobsTableColumn.JobId -> 80.dp
        JobsTableColumn.QueueName -> 120.dp
        JobsTableColumn.Status -> 120.dp
        JobsTableColumn.Priority -> 80.dp
        JobsTableColumn.Attempts -> 80.dp
        JobsTableColumn.InsertedAt -> 120.dp
        JobsTableColumn.RunAt -> 80.dp
        JobsTableColumn.DeduplicationKey -> 300.dp
        JobsTableColumn.Lease -> 120.dp
        JobsTableColumn.RunningDuration -> 80.dp
        JobsTableColumn.LastRunFinishedAt -> 80.dp
        JobsTableColumn.LastRunDuration -> 80.dp
        JobsTableColumn.LastRunResult -> 120.dp
        JobsTableColumn.Payload -> 80.dp
        JobsTableColumn.ResultData -> 80.dp
        JobsTableColumn.State -> 80.dp
    }

val JobsTableCell.colors: Colors
    @Composable
    get() = this.job?.metadata?.status?.colors ?: Colors.surface

val DatabaseJobStatus.colors: Colors
    @Composable
    get() = when (this) {
        DatabaseJobStatus.Pending -> Colors.yellow
        DatabaseJobStatus.Running -> Colors.purple
        DatabaseJobStatus.Succeeded -> Colors.green
        DatabaseJobStatus.Failed -> Colors.red
        DatabaseJobStatus.Cooldown -> Colors.blue
        DatabaseJobStatus.Cancelled -> Colors.gray
    }

val QueueName.colors: Colors
    @Composable
    get() = when (this) {
        QueueName.High -> Colors.red
        QueueName.Default -> Colors.blue
        QueueName.Low -> Colors.gray
    }

val JobCompletionResultType.colors: Colors
    @Composable
    get() = when (this) {
        JobCompletionResultType.Success -> Colors.green
        JobCompletionResultType.Cancelled -> Colors.blue
        JobCompletionResultType.Timeout -> Colors.orange
        JobCompletionResultType.Failure -> Colors.red
    }

@Composable
fun RenderJobsTableCell(
    cell: JobsTableCell,
    json: Json,
    currentTime: Instant,
    uiState: MainScreenContract.State,
    postInput: (MainScreenContract.Inputs) -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(cell.colors.backgroundColor)
            .border(Dp.Hairline, MaterialTheme.colorScheme.onSurface)
    ) {
        CompositionLocalProvider(LocalContentColor provides cell.colors.contentColor) {
            if (cell.job == null) {
                RenderJobsTableCellHeader(
                    column = cell.column,
                    uiState = uiState,
                    postInput = postInput,
                )
            } else {
                RenderJobsTableCellValue(
                    job = cell.job,
                    column = cell.column,
                    json = json,
                    currentTime = currentTime,
                    uiState = uiState,
                    postInput = postInput,
                )
            }
        }
    }
}

@Composable
fun RenderJobsTableCellHeader(
    column: JobsTableColumn,
    uiState: MainScreenContract.State,
    postInput: (MainScreenContract.Inputs) -> Unit,
) {
    when (column) {
        JobsTableColumn.ViewDetailsButton -> Box { }
        JobsTableColumn.ActionsMenu -> Text(
            "Actions",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )

        JobsTableColumn.ToggleSelection -> {
            AnimatedContent(
                targetState = uiState.selectedJobs.isNotEmpty(),
            ) { hasJobsSelected ->
                if (hasJobsSelected) {
                    JobDropdownMenu(
                        null,
                        enabled = true,
                        postInput,
                    )
                } else {
                    Checkbox(
                        checked = false,
                        onCheckedChange = { postInput(MainScreenContract.Inputs.ToggleAllRowSelection(true)) }
                    )
                }
            }
        }

        JobsTableColumn.Attempts -> Text(
            "Attempts",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )

        JobsTableColumn.InsertedAt -> Text(
            "Inserted At",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )

        JobsTableColumn.JobId -> Text(
            "JobId",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )

        JobsTableColumn.Priority -> Text(
            "Priority",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )

        JobsTableColumn.QueueName -> Text(
            "Queue Name",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )

        JobsTableColumn.RunAt -> Text(
            "Run At",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )

        JobsTableColumn.Status -> Text(
            "Status",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )

        JobsTableColumn.DeduplicationKey -> Text(
            "Deduplication Key",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )

        JobsTableColumn.Lease -> Text(
            "Lease",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )

        JobsTableColumn.RunningDuration -> Text(
            "Running Duration",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )

        JobsTableColumn.LastRunFinishedAt -> Text(
            "Last Run Finished At",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )

        JobsTableColumn.LastRunDuration -> Text(
            "Last Run Duration",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )

        JobsTableColumn.LastRunResult -> Text(
            "Last Run Result",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )

        JobsTableColumn.Payload -> Text(
            "Payload",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )

        JobsTableColumn.ResultData -> Text(
            "ResultData",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )

        JobsTableColumn.State -> Text(
            "State",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun RenderJobsTableCellValue(
    job: SerializedJob<DatabaseQueueDriver.Metadata>,
    column: JobsTableColumn,
    json: Json,
    currentTime: Instant,
    uiState: MainScreenContract.State,
    postInput: (MainScreenContract.Inputs) -> Unit,
) = with(job) {
    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodySmall) {
        when (column) {
            JobsTableColumn.ViewDetailsButton -> {
                Button({ postInput(MainScreenContract.Inputs.ViewJobDetails(job.jobId)) }) {
                    Text("View Details")
                }
            }

            JobsTableColumn.ActionsMenu -> {
                JobDropdownMenu(job, true, postInput)
            }

            JobsTableColumn.ToggleSelection -> {
                Checkbox(
                    checked = job.jobId in uiState.selectedJobs,
                    onCheckedChange = {
                        postInput(MainScreenContract.Inputs.ToggleRowSelection(job.jobId))
                    }
                )
            }

            JobsTableColumn.Attempts -> Text("${job.metadata.attempts}/${job.metadata.maxAttempts}")
            JobsTableColumn.InsertedAt -> Text(job.metadata.insertedAt.formatted)
            JobsTableColumn.JobId -> Text(job.jobId)
            JobsTableColumn.Priority -> Text("${job.metadata.priority}")
            JobsTableColumn.QueueName -> {
                val colors = QueueName.valueOf(job.queueName).colors
                SuggestionChip(
                    onClick = {},
                    label = { Text(job.queueName) },
                    colors = SuggestionChipDefaults.suggestionChipColors().copy(
                        containerColor = colors.backgroundColor,
                        labelColor = colors.contentColor,
                    )
                )
            }

            JobsTableColumn.RunAt -> Text(job.metadata.runAt.formatted)
            JobsTableColumn.Status -> {
                val colors = job.metadata.status.colors
                SuggestionChip(
                    onClick = {},
                    label = { Text(job.metadata.status.name) },
                    colors = SuggestionChipDefaults.suggestionChipColors().copy(
                        containerColor = colors.backgroundColor,
                        labelColor = colors.contentColor,
                    )
                )
            }

            JobsTableColumn.DeduplicationKey -> {
                if (job.metadata.deduplicationKey != null) {
                    Text("${job.metadata.deduplicationKey} (for ${job.metadata.deduplicationDuration?.formatted})")
                } else {
                    Text("N/A")
                }
            }

            JobsTableColumn.Lease -> {
                val leasedAt = job.metadata.leasedAt
                val leasedUntil = job.metadata.leasedUntil
                if (leasedAt != null && leasedUntil != null) {
                    if (currentTime in leasedAt..leasedUntil) {
                        Text(
                            "Lease expires in\n${(leasedUntil - currentTime).formatted}",
                            color = Colors.purple.contentColor,
                            textAlign = TextAlign.Center,
                        )
                    } else if (currentTime > leasedUntil) {
                        Text(
                            "Lease expired at\n${leasedUntil.formatted}",
                            color = Colors.red.contentColor,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        Text("N/A")
                    }
                } else {
                    Text("N/A")
                }
            }

            JobsTableColumn.RunningDuration -> {
                if (job.metadata.status == DatabaseJobStatus.Running && job.metadata.leasedAt != null) {
                    val runningDuration = currentTime - job.metadata.leasedAt!!
                    Text(runningDuration.formatted)
                } else {
                    Text("N/A")
                }
            }

            JobsTableColumn.LastRunFinishedAt -> Text(job.metadata.lastRunFinishedAt?.formatted ?: "N/A")
            JobsTableColumn.LastRunDuration -> Text(job.metadata.lastRunDuration?.formatted ?: "N/A")
            JobsTableColumn.LastRunResult -> {
                if (job.metadata.lastResultType != null) {
                    val colors = job.metadata.lastResultType!!.colors
                    SuggestionChip(
                        onClick = {},
                        label = { Text(job.metadata.lastResultType!!.name) },
                        colors = SuggestionChipDefaults.suggestionChipColors().copy(
                            containerColor = colors.backgroundColor,
                            labelColor = colors.contentColor,
                        )
                    )
                } else {
                    Text("N/A")
                }
            }

            JobsTableColumn.Payload -> JsonTreeView(job.serializedPayload, json)
            JobsTableColumn.ResultData -> JsonTreeView(job.serializedResultData, json)
            JobsTableColumn.State -> JsonTreeView(job.serializedState, json)
        }
    }
}
