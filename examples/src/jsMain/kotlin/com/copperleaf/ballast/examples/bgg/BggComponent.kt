package com.copperleaf.ballast.examples.bgg

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.examples.util.Component
import com.copperleaf.ballast.examples.util.ComposeWebInjector

class BggComponent(
    private val injector: ComposeWebInjector
) : Component {
    @Composable
    override fun Content() {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope) { injector.bggViewModel(viewModelCoroutineScope) }
        val uiState by vm.observeStates().collectAsState()

        BggWebUi.Content(uiState) { vm.trySend(it) }
    }
}
