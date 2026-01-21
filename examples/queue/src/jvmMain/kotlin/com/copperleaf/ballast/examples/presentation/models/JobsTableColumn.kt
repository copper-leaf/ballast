package com.copperleaf.ballast.examples.presentation.models

sealed class JobsTableColumn {
    object ViewDetailsButton : JobsTableColumn()
    object ActionsMenu : JobsTableColumn()
    object ToggleSelection : JobsTableColumn()

    object JobId : JobsTableColumn()
    object QueueName : JobsTableColumn()
    object Status : JobsTableColumn()
    object Priority : JobsTableColumn()
    object Attempts : JobsTableColumn()
    object RunAt : JobsTableColumn()
    object InsertedAt : JobsTableColumn()
    object DeduplicationKey : JobsTableColumn()
    object Lease : JobsTableColumn()
    object LastRunFinishedAt : JobsTableColumn()
    object LastRunDuration : JobsTableColumn()
    object LastRunResult : JobsTableColumn()
    object RunningDuration : JobsTableColumn()

    object Payload : JobsTableColumn()
    object State : JobsTableColumn()
    object ResultData : JobsTableColumn()

    companion object {
        fun defaultTableColumns(): List<JobsTableColumn> {
            return listOf(
                ToggleSelection,
                QueueName,
                Status,
                RunAt,
                Attempts,
                Lease,
                RunningDuration,
                LastRunResult,
                LastRunDuration,
                ActionsMenu,
                ViewDetailsButton,
            )
        }

        fun defaultDetailsColumns(): List<JobsTableColumn> {
            return listOf(
                QueueName,
                Status,
                Priority,
                InsertedAt,
                RunAt,
                Attempts,
                LastRunFinishedAt,
                LastRunDuration,
                LastRunResult,
                Payload,
                State,
                ResultData,
            )
        }
    }
}
