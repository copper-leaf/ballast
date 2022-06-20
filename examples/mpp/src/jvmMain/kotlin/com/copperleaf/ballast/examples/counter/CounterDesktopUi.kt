package com.copperleaf.ballast.examples.counter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.copperleaf.ballast.examples.util.LocalInjector
import com.copperleaf.ballast.sync.SyncClientType

object CounterDesktopUi {
    @Composable
    fun DesktopContent() {
        val injector = LocalInjector.current

        // source VM
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope) {
            injector.counterViewModel(
                viewModelCoroutineScope,
                SyncClientType.Source
            )
        }
        val uiState by vm.observeStates().collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { vm.trySend(CounterContract.Inputs.GoBack) }) {
                            Icon(Icons.Default.ArrowBack, "")
                        }
                    },
                    title = { Text("BGG") },
                )
            },
            content = {
                Column(
                    modifier = Modifier.padding(it).padding(16.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Source")
                    CounterComposeUi.Content(uiState) { input -> vm.trySend(input) }
                    Divider()

                    Text("Replicas")
                    repeat(3) { replicaIndex ->
                        ReplicaViewModelUi(replicaIndex, SyncClientType.Replica)
                    }

                    Text("Spectators")
                    repeat(3) { spectatorIndex ->
                        ReplicaViewModelUi(spectatorIndex + 3, SyncClientType.Spectator)
                    }
                }
            }
        )
    }

    @Composable
    private fun ReplicaViewModelUi(index: Int, syncClientType: SyncClientType) {
        Card(elevation = 4.dp) {
            Column {
                val injector = LocalInjector.current
                val replicaViewModelCoroutineScope = rememberCoroutineScope()

                // Replica VM 1
                val replicaVm = remember(replicaViewModelCoroutineScope) {
                    injector.counterViewModel(
                        replicaViewModelCoroutineScope,
                        syncClientType,
                    )
                }
                val replicaUiState by replicaVm.observeStates().collectAsState()

                Text("Replica VM $index")
                CounterComposeUi.Content(replicaUiState) { input -> replicaVm.trySend(input) }
            }
        }
    }
}
