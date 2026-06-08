package com.copperleaf.ballast.examples.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SyncProblem
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

@Composable
fun JobsTableDropdownMenu(
    postInput: (MainScreenContract.Inputs) -> Unit,
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    IconButton({ isMenuOpen = true }) {
        Icon(Icons.Default.MoreVert, "Toggle Main Menu")
    }
    DropdownMenu(
        expanded = isMenuOpen,
        onDismissRequest = { isMenuOpen = false }
    ) {
        DropdownMenuItem(
            onClick = {
                postInput(MainScreenContract.Inputs.DeleteOldJobs)
                isMenuOpen = false
            },
            leadingIcon = { Icon(Icons.Filled.DeleteSweep, "Delete old jobs") },
            text = { Text("Delete old jobs") },
        )
        DropdownMenuItem(
            onClick = {
                postInput(MainScreenContract.Inputs.FreeJobCooldowns)
                isMenuOpen = false
            },
            leadingIcon = { Icon(Icons.Filled.HourglassBottom, "Free jobs cooldowns") },
            text = { Text("Free jobs cooldowns") },
        )
        DropdownMenuItem(
            onClick = {
                postInput(MainScreenContract.Inputs.RetryHungJobs)
                isMenuOpen = false
            },
            leadingIcon = { Icon(Icons.Filled.SyncProblem, "Retry hung jobs") },
            text = { Text("Retry hung jobs") },
        )
    }
}
