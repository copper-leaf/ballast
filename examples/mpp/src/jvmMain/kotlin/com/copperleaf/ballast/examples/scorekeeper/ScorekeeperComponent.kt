package com.copperleaf.ballast.examples.scorekeeper

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.examples.util.Component
import com.copperleaf.ballast.examples.util.ComposeDesktopInjector

class ScorekeeperComponent(
    private val injector: ComposeDesktopInjector
) : Component {
    private val snackbarHostState = SnackbarHostState()

    @Composable
    override fun Content() {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope, snackbarHostState) {
            injector.scorekeeperViewModel(viewModelCoroutineScope, snackbarHostState)
        }
        val uiState by vm.observeStates().collectAsState()

        ScorekeeperComposeUi.Content(snackbarHostState, uiState) { vm.trySend(it) }
    }
}
