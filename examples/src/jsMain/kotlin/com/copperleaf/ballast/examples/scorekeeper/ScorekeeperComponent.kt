package com.copperleaf.ballast.examples.scorekeeper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.examples.util.Component
import com.copperleaf.ballast.examples.util.ComposeWebInjector

class ScorekeeperComponent(
    private val injector: ComposeWebInjector
) : Component {
    @Composable
    override fun Content() {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope) { injector.scorekeeperViewModel(viewModelCoroutineScope) }
        val uiState by vm.observeStates().collectAsState()

        ScorekeeperWebUi.Content(uiState) { vm.trySend(it) }
    }
}
