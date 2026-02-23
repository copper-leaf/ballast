package com.copperleaf.ballast.examples.scheduler.persistent

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@ExperimentalMaterial3Api
object PersistentSchedulesUi {

    @Composable
    fun Content(scope: ColumnScope) = with(scope) {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm: PersistentSchedulesViewModel = remember(viewModelCoroutineScope) {
            PersistentSchedulesViewModel(viewModelCoroutineScope)
        }
        val uiState by vm.observeStates().collectAsState()

        Content(uiState) { vm.trySend(it) }
    }

    @Composable
    public fun ColumnScope.Content(
        uiState: PersistentSchedulesContract.State,
        postInput: (PersistentSchedulesContract.Inputs) -> Unit,
    ) {
        Button({ postInput(PersistentSchedulesContract.Inputs.StartSchedule) }) {
            Text("Start schedule")
        }

        Button({ postInput(PersistentSchedulesContract.Inputs.StopSchedule) }) {
            Text("Stop schedule")
        }

        Button({ postInput(PersistentSchedulesContract.Inputs.SendTestNotification) }) {
            Text("Send Test Notification")
        }

        HorizontalDivider()

        Text("Notification logs:")
        uiState.logs.forEach {
            Text(it, modifier = Modifier.padding(all = 8.dp))
        }
    }
}
