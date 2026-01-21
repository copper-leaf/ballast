package com.copperleaf.ballast.examples.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.copperleaf.ballast.examples.presentation.ui.MainScreenContract
import com.copperleaf.ballast.queue.SerializedJob
import com.copperleaf.ballast.queue.driver.DatabaseQueueDriver

@Composable
fun JobDropdownMenu(
    job: SerializedJob<DatabaseQueueDriver.Metadata>?,
    enabled: Boolean,
    postInput: (MainScreenContract.Inputs) -> Unit,
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    IconButton({ isMenuOpen = true }, enabled = enabled) {
        Icon(Icons.Default.MoreVert, "Toggle Job Menu")
    }
    DropdownMenu(
        expanded = isMenuOpen,
        onDismissRequest = { isMenuOpen = false }
    ) {
        DropdownMenuItem(
            onClick = {
                postInput(MainScreenContract.Inputs.CancelJob(job?.jobId))
                isMenuOpen = false
            },
            leadingIcon = { Icon(Icons.Filled.Cancel, "Cancel job") },
            text = { Text("Cancel job") },
        )
        DropdownMenuItem(
            onClick = {
                postInput(MainScreenContract.Inputs.DeleteJob(job?.jobId))
                isMenuOpen = false
            },
            leadingIcon = { Icon(Icons.Filled.Delete, "Delete job") },
            text = { Text("Delete job") },
        )
        DropdownMenuItem(
            onClick = {
                postInput(MainScreenContract.Inputs.ForceRetry(job?.jobId))
                isMenuOpen = false
            },
            leadingIcon = { Icon(Icons.Filled.Replay, "Force retry") },
            text = { Text("Force retry") },
        )

        if (job == null) {
            DropdownMenuItem(
                onClick = {
                    postInput(MainScreenContract.Inputs.ToggleAllRowSelection(false))
                    isMenuOpen = false
                },
                leadingIcon = { Icon(Icons.Filled.ClearAll, "Deselect all") },
                text = { Text("Deselect all") },
            )
        }
    }
}
