package com.copperleaf.ballast.debugger.idea.tool

import androidx.compose.runtime.Composable
import com.copperleaf.ballast.debugger.idea.base.ComposeToolWindow
import com.copperleaf.ballast.debugger.ui.debugger.DebuggerUi

class BallastDebuggerToolWindow : ComposeToolWindow() {

    @Composable
    override fun Content() {
        DebuggerUi.run()
    }
}
