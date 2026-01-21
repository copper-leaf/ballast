package com.copperleaf.ballast.examples.presentation.models

import com.copperleaf.ballast.queue.SerializedJob
import com.copperleaf.ballast.queue.driver.DatabaseQueueDriver

data class JobsTableCell(
    val job: SerializedJob<DatabaseQueueDriver.Metadata>?, // null indicates header row
    val column: JobsTableColumn,
    val rowIndex: Int,
    val columnIndex: Int,
)
