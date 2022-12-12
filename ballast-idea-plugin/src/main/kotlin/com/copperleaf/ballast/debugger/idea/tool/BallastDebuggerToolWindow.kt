package com.copperleaf.ballast.debugger.idea.tool

import androidx.compose.runtime.Composable
import com.copperleaf.ballast.debugger.idea.base.ComposableContent
import com.copperleaf.ballast.debugger.idea.base.ComposableToolWindowFactory
import com.copperleaf.ballast.debugger.ui.debugger.DebuggerUi
import com.intellij.openapi.project.DumbAware

class BallastDebuggerToolWindow {

    class Factory : ComposableToolWindowFactory, DumbAware {
        override fun provideComposableContent(): Contents {
            return Contents()
        }
    }

    class Contents : ComposableContent {
        @Composable
        override fun Content() {
            DebuggerUi.run()
        }
    }

}
