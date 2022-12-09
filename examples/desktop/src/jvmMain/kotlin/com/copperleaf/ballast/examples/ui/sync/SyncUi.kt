package com.copperleaf.ballast.examples.ui.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.copperleaf.ballast.examples.ui.counter.CounterContract
import com.copperleaf.ballast.sync.DefaultSyncConnection
import com.copperleaf.ballast.sync.InMemorySyncAdapter
import com.copperleaf.ballast.sync.SyncConnectionAdapter

object SyncUi {

    @Composable
    fun Content(injector: ComposeDesktopInjector) {
        val syncAdapter = remember(injector) {
            InMemorySyncAdapter<
                    CounterContract.Inputs,
                    CounterContract.Events,
                    CounterContract.State>()
        }

        Column(Modifier.fillMaxSize()) {
            TopAppBar(title = { Text("Sync") })
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Source", style = MaterialTheme.typography.subtitle1,
                )
                SyncedViewModelUi(injector, 0, DefaultSyncConnection.ClientType.Source, syncAdapter)
                Spacer(Modifier.height(20.dp))

                Text(
                    "Replicas", style = MaterialTheme.typography.subtitle1,
                )
                repeat(3) { replicaIndex ->
                    SyncedViewModelUi(injector, replicaIndex, DefaultSyncConnection.ClientType.Replica, syncAdapter)
                }
                Spacer(Modifier.height(20.dp))

                Text(
                    "Spectators", style = MaterialTheme.typography.subtitle1,
                )
                repeat(3) { spectatorIndex ->
                    SyncedViewModelUi(
                        injector,
                        spectatorIndex + 3,
                        DefaultSyncConnection.ClientType.Spectator,
                        syncAdapter
                    )
                }
            }
        }
    }


    @Composable
    private fun ColumnScope.SyncedViewModelUi(
        injector: ComposeDesktopInjector,
        index: Int,
        syncClientType: DefaultSyncConnection.ClientType,
        syncAdapter: SyncConnectionAdapter<
                CounterContract.Inputs,
                CounterContract.Events,
                CounterContract.State>?,
    ) {
        val coroutineScope = rememberCoroutineScope()
        val replicaVm = remember(injector, coroutineScope) {
            injector.counterViewModel(coroutineScope, syncClientType, syncAdapter)
        }
        val replicaUiState by replicaVm.observeStates().collectAsState()
        Text(
            "${syncClientType.name} Counter${if (index > 0) " $index" else ""}",
            style = MaterialTheme.typography.subtitle1,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FloatingActionButton(
                onClick = { replicaVm.trySend(CounterContract.Inputs.Decrement(1)) }
            ) {
                Icon(Icons.Default.Remove, "Decrement")
            }

            Text(
                text = "${replicaUiState.count}",
                style = MaterialTheme.typography.h3,
            )

            FloatingActionButton(
                onClick = { replicaVm.trySend(CounterContract.Inputs.Increment(1)) }
            ) {
                Icon(Icons.Default.Add, "Increment")
            }
        }
    }
}
