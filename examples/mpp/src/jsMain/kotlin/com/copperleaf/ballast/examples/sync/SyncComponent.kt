package com.copperleaf.ballast.examples.sync

import androidx.compose.runtime.Composable
import com.copperleaf.ballast.examples.util.Component
import com.copperleaf.ballast.examples.util.ComposeWebInjector

class SyncComponent(
    private val injector: ComposeWebInjector
) : Component {
    @Composable
    override fun Content() {
        SyncWebUi.Content(injector)
    }
}
