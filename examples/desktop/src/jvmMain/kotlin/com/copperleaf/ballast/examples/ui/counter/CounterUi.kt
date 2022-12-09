package com.copperleaf.ballast.examples.ui.counter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.examples.injector.ComposeDesktopInjector

object CounterUi {

    @Composable
    fun Content(injector: ComposeDesktopInjector) {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope) { injector.counterViewModel(viewModelCoroutineScope, null, null) }
        val uiState by vm.observeStates().collectAsState()

        Content(uiState) { vm.trySend(it) }
    }

    @Composable
    public fun Content(
        uiState: CounterContract.State,
        postInput: (CounterContract.Inputs) -> Unit,
    ) {
        Column {
            TopAppBar(title = { Text("Counter") })
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FloatingActionButton(
                    onClick = { postInput(CounterContract.Inputs.Decrement(1)) }
                ) {
                    Icon(Icons.Default.Remove, "Decrement")
                }

                Text(
                    text = "${uiState.count}",
                    style = MaterialTheme.typography.h3,
                )

                FloatingActionButton(
                    onClick = { postInput(CounterContract.Inputs.Increment(1)) }
                ) {
                    Icon(Icons.Default.Add, "Increment")
                }
            }
        }
    }

}
