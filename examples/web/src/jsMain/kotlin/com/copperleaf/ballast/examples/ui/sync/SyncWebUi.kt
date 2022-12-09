package com.copperleaf.ballast.examples.ui.sync

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.examples.injector.ComposeWebInjector
import com.copperleaf.ballast.examples.ui.counter.CounterContract
import com.copperleaf.ballast.examples.ui.util.bulma.BulmaButton
import com.copperleaf.ballast.examples.ui.util.bulma.BulmaButtonGroup
import com.copperleaf.ballast.examples.ui.util.bulma.BulmaColor
import com.copperleaf.ballast.sync.DefaultSyncConnection
import com.copperleaf.ballast.sync.InMemorySyncAdapter
import com.copperleaf.ballast.sync.SyncConnectionAdapter
import org.jetbrains.compose.web.dom.B
import org.jetbrains.compose.web.dom.Text

object SyncWebUi {
    @Composable
    public fun Content(injector: ComposeWebInjector) {
        val syncAdapter = remember(injector) {
            InMemorySyncAdapter<
                    CounterContract.Inputs,
                    CounterContract.Events,
                    CounterContract.State>()
        }

        Text("Source")
        SyncedViewModelUi(injector, 0, DefaultSyncConnection.ClientType.Source, syncAdapter)

        Text("Replicas")
        repeat(3) { replicaIndex ->
            SyncedViewModelUi(injector, replicaIndex, DefaultSyncConnection.ClientType.Replica, syncAdapter)
        }

        Text("Spectators")
        repeat(3) { spectatorIndex ->
            SyncedViewModelUi(injector, spectatorIndex + 3, DefaultSyncConnection.ClientType.Spectator, syncAdapter)
        }
    }

    @Composable
    private fun SyncedViewModelUi(
        injector: ComposeWebInjector,
        index: Int,
        syncClientType: DefaultSyncConnection.ClientType,
        syncAdapter: SyncConnectionAdapter<
                CounterContract.Inputs,
                CounterContract.Events,
                CounterContract.State>,
    ) {
        val replicaViewModelCoroutineScope = rememberCoroutineScope()

        val replicaVm = remember(replicaViewModelCoroutineScope) {
            injector.counterViewModel(
                replicaViewModelCoroutineScope,
                syncClientType,
                syncAdapter,
            )
        }
        val replicaUiState by replicaVm.observeStates().collectAsState()
        B { Text("${syncClientType.name} Counter${if (index > 0) " $index" else ""}") }
        BulmaButtonGroup {
            Control {
                BulmaButton(onClick = { replicaVm.trySend(CounterContract.Inputs.Decrement(1)) }) {
                    Text("-")
                }
            }
            Control {
                BulmaButton(
                    onClick = { },
                    color = BulmaColor.Default
                ) {
                    Text("${replicaUiState.count}")
                }
            }
            Control {
                BulmaButton(onClick = { replicaVm.trySend(CounterContract.Inputs.Increment(1)) }) {
                    Text("+")
                }
            }
        }
    }
}
