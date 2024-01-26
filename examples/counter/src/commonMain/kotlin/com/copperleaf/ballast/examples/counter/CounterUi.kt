package com.copperleaf.ballast.examples.counter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@ExperimentalMaterial3Api
object CounterUi {

    @Composable
    fun Content() {
        val snackbarHostState = remember { SnackbarHostState() }
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm: CounterViewModel = remember(viewModelCoroutineScope, snackbarHostState) {
            createViewModel(
                viewModelCoroutineScope,
                snackbarHostState
            )
        }
        val uiState by vm.observeStates().collectAsState()

        Content(snackbarHostState, uiState) { vm.trySend(it) }
    }

    @Composable
    public fun Content(
        snackbarHostState: SnackbarHostState,
        uiState: CounterContract.State,
        postInput: (CounterContract.Inputs) -> Unit,
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
                FloatingActionButton(
                    onClick = { postInput(CounterContract.Inputs.Decrement(1)) }
                ) {
                    Icon(Icons.Default.Remove, "Decrement")
                }

                Text(
                    text = "${uiState.count}",
                    style = MaterialTheme.typography.headlineLarge,
                )

                FloatingActionButton(
                    onClick = { postInput(CounterContract.Inputs.Increment(1)) }
                ) {
                    Icon(Icons.Default.Add, "Increment")
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
