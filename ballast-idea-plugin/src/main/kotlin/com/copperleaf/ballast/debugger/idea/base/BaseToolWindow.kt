package com.copperleaf.ballast.debugger.idea.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposePanel
import com.copperleaf.ballast.debugger.idea.theme.IdeaPluginTheme
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory

abstract class BaseToolWindow : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content: Content = contentFactory.createContent(
            ComposePanel().apply {
                setContent {
                    IdeaPluginTheme(project) {
                        Content()
                    }
                }
            },
            "",
            false,
        )
        toolWindow.contentManager.addContent(content)
    }

    @Composable
    abstract fun Content()
}
