package com.copperleaf.ballast.examples.sync

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.examples.util.Component
import com.copperleaf.ballast.examples.util.ComposeDesktopInjector

class SyncComponent(
    private val injector: ComposeDesktopInjector
) : Component {
    @Composable
    override fun Content() {
        val coroutineScope = rememberCoroutineScope()
        SyncComposeUi.Content { injector.counterViewModel(coroutineScope, it) }
    }
}
