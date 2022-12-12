package com.copperleaf.ballast.debugger.idea.base

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory

interface ComposableToolWindowFactory : ToolWindowFactory, ComposableContent.Provider {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content: Content = contentFactory.createContent(getComposePanel(project), "", false)
        toolWindow.contentManager.addContent(content)
    }
}
