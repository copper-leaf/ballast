package com.copperleaf.ballast.examples.sync

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.examples.counter.CounterContract
import com.copperleaf.ballast.examples.util.ComposeWebInjector
import com.copperleaf.ballast.examples.util.ExamplesContext
import com.copperleaf.ballast.examples.util.bulma.BulmaButton
import com.copperleaf.ballast.examples.util.bulma.BulmaButtonGroup
import com.copperleaf.ballast.examples.util.bulma.BulmaColor
import com.copperleaf.ballast.examples.util.bulma.BulmaPanel
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.B
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Text
import com.copperleaf.ballast.sync.DefaultSyncConnection

object SyncWebUi {
    @Composable
    public fun Content(injector: ComposeWebInjector) {
        BulmaPanel(
            headingStart = { Text("Sync") },
            headingEnd = {
                A(
                    href = "${ExamplesContext.samplesUrlWithVersion}/sync",
                    attrs = {
                        title("View sources on GitHub")
                        target(ATarget.Blank)
                    }
                ) {
                    I { Text("Sources") }
                }
            },
        ) {
            Text("Source")
            SyncedViewModelUi(injector, 0, DefaultSyncConnection.ClientType.Source)

            Text("Replicas")
            repeat(3) { replicaIndex ->
                SyncedViewModelUi(injector, replicaIndex, DefaultSyncConnection.ClientType.Replica)
            }

            Text("Spectators")
            repeat(3) { spectatorIndex ->
                SyncedViewModelUi(injector, spectatorIndex + 3, DefaultSyncConnection.ClientType.Spectator)
            }
        }
    }

    @Composable
    private fun SyncedViewModelUi(
        injector: ComposeWebInjector,
        index: Int,
        syncClientType: DefaultSyncConnection.ClientType
    ) {
        val replicaViewModelCoroutineScope = rememberCoroutineScope()

        val replicaVm = remember(replicaViewModelCoroutineScope) {
            injector.counterViewModel(
                replicaViewModelCoroutineScope,
                syncClientType,
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
