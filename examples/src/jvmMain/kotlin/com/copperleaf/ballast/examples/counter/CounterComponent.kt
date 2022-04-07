package com.copperleaf.ballast.examples.counter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.examples.util.Component
import com.copperleaf.ballast.examples.util.ComposeDesktopInjector

class CounterComponent(
    private val injector: ComposeDesktopInjector
) : Component {
    @Composable
    override fun Content() {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope) { injector.counterViewModel(viewModelCoroutineScope) }
        val uiState by vm.observeStates().collectAsState()

        CounterComposeUi.Content(uiState) { vm.trySend(it) }
    }
}
