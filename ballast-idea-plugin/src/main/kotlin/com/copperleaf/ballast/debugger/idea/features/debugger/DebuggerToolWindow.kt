package com.copperleaf.ballast.debugger.idea.features.debugger

import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.debugger.idea.BallastIntellijPluginInjector
import com.copperleaf.ballast.debugger.idea.base.setContent
import com.copperleaf.ballast.debugger.idea.features.debugger.ui.DebuggerUi
import com.copperleaf.ballast.debugger.idea.theme.IdeaPluginTheme
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class DebuggerToolWindow {
    class Factory : ToolWindowFactory, DumbAware {
        override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
            toolWindow.setContent(null) {
                val toolWindowCoroutineScope = rememberCoroutineScope()
                val injector = remember {
                    DebuggerToolWindowInjectorImpl(
                        pluginInjector = BallastIntellijPluginInjector.getInstance(project),
                        toolWindowCoroutineScope = toolWindowCoroutineScope,
                    )
                }
                IdeaPluginTheme(project) {
                    DebuggerUi.Content(injector)
                }
            }
        }
    }
}
