package com.copperleaf.ballast.examples.counter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.examples.util.ExamplesContext
import com.copperleaf.ballast.examples.util.LocalInjector
import com.copperleaf.ballast.examples.util.bulma.BulmaButton
import com.copperleaf.ballast.examples.util.bulma.BulmaButtonGroup
import com.copperleaf.ballast.examples.util.bulma.BulmaColor
import com.copperleaf.ballast.examples.util.bulma.BulmaPanel
import com.copperleaf.ballast.sync.SyncClientType
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Text

object CounterWebUi {
    @Composable
    public fun WebContent(synced: Boolean) {
        val injector = LocalInjector.current

        // source VM
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope) {
            injector.counterViewModel(
                viewModelCoroutineScope,
                SyncClientType.Source,
            )
        }
        val uiState by vm.observeStates().collectAsState()

        Text("Source")
        BulmaPanel(
            headingStart = {
                if (synced) {
                    Text("Source Counter")
                } else {
                    Text("Counter")
                }
            },
            headingEnd = {
                A(
                    href = "${ExamplesContext.samplesUrlWithVersion}/counter",
                    attrs = {
                        title("View sources on GitHub")
                        target(ATarget.Blank)
                    }
                ) {
                    I { Text("Sources") }
                }
            },
            color = BulmaColor.Primary,
        ) {
            BulmaButtonGroup {
                Control {
                    BulmaButton(onClick = { vm.trySend(CounterContract.Inputs.Decrement(1)) }) {
                        Text("-")
                    }
                }
                Control {
                    BulmaButton(
                        onClick = { },
                        color = BulmaColor.Default
                    ) {
                        Text("${uiState.count}")
                    }
                }
                Control {
                    BulmaButton(onClick = { vm.trySend(CounterContract.Inputs.Increment(1)) }) {
                        Text("+")
                    }
                }
            }
        }

        if (synced) {
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

    @Composable
    private fun ReplicaViewModelUi(index: Int, syncClientType: SyncClientType) {
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

        BulmaPanel(
            headingStart = { Text("${syncClientType.name} Counter $index") },
            color = BulmaColor.Default,
        ) {
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
}
