package com.copperleaf.ballast.debugger.idea.tool

import androidx.compose.runtime.Composable
import com.copperleaf.ballast.debugger.idea.base.ComposeToolWindow
import com.copperleaf.ballast.debugger.ui.debugger.DebuggerUi
import com.intellij.openapi.project.DumbAware

class BallastDebuggerToolWindow : ComposeToolWindow(), DumbAware {

    @Composable
    override fun Content() {
        DebuggerUi.run()
    }
}
