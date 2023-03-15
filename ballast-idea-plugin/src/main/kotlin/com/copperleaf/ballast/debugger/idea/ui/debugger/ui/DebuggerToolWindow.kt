package com.copperleaf.ballast.debugger.idea.ui.debugger.ui

import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.debugger.idea.base.setContent
import com.copperleaf.ballast.debugger.idea.ui.debugger.injector.DebuggerToolWindowInjector
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class DebuggerToolWindow {
    class Factory : ToolWindowFactory, DumbAware {
        override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
            toolWindow.setContent(null) {
                val toolWindowCoroutineScope = rememberCoroutineScope()
                val injector = remember { DebuggerToolWindowInjector.getInstance(project, toolWindowCoroutineScope) }
                DebuggerUi.Content(injector)
            }
        }
    }
}
