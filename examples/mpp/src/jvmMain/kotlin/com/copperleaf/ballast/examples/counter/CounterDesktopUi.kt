package com.copperleaf.ballast.examples.counter

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
import com.copperleaf.ballast.examples.util.LocalInjector

object CounterDesktopUi {
    @Composable
    fun DesktopContent() {
        val injector = LocalInjector.current

        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope) { injector.counterViewModel(viewModelCoroutineScope) }
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
                CounterComposeUi.Content(uiState) { vm.trySend(it) }
            }
        )
    }
}
