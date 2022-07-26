package com.copperleaf.ballast.examples.counter

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.copperleaf.ballast.examples.util.LocalInjector
import com.copperleaf.ballast.examples.util.ballastViewModelFactory
import com.copperleaf.ballast.sync.SyncClientType

object CounterAndroidUi {

    @Composable
    fun AndroidContent() {
        val injector = LocalInjector.current
        val vm: CounterViewModel = viewModel(
            factory = ballastViewModelFactory()
        )

        val uiState by vm.observeStates().collectAsState()

        LaunchedEffect(vm) {
            vm.attachEventHandler(this, CounterEventHandler(injector.routerViewModel()))
        }

        BackHandler { vm.trySend(CounterContract.Inputs.GoBack) }

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { vm.trySend(CounterContract.Inputs.GoBack) }) {
                            Icon(Icons.Default.ArrowBack, "")
                        }
                    },
                    title = { Text("Counter") },
                )
            },
            content = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Source")
                    Card {
                        Column(Modifier.padding(16.dp)) {
                            CounterComposeUi.Content(uiState) { vm.trySend(it) }
                        }
                    }
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
            Column(Modifier.padding(16.dp)) {
                val injector = LocalInjector.current

                // Replica VM 1
                val replicaVm: CounterViewModel = remember {
                    injector.counterViewModel(SavedStateHandle(), syncClientType)
                }
                val replicaUiState by replicaVm.observeStates().collectAsState()

                Text("Replica VM $index")
                CounterComposeUi.Content(replicaUiState) { input -> replicaVm.trySend(input) }
            }
        }
    }
}
