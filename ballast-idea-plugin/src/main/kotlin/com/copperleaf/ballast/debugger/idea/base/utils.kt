@file:Suppress("UNUSED_PARAMETER")
package com.copperleaf.ballast.debugger.idea.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import com.copperleaf.ballast.debugger.idea.theme.IdeaPluginTheme
import com.copperleaf.ballast.debugger.idea.ui.debugger.widgets.LocalProject
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import javax.swing.JComponent

fun ToolWindow.setContent(
    displayName: String?,
    isLockable: Boolean = true,
    content: @Composable () -> Unit
) = BallastComposePanel(project = project, content = content)
    .also { contentManager.addContent(contentManager.factory.createContent(it, displayName, isLockable)) }

fun BallastComposePanel(
    project: Project,
    height: Int = 800,
    width: Int = 800,
    y: Int = 0,
    x: Int = 0,
    content: @Composable () -> Unit
): JComponent {
    return ComposePanel().apply {
        setContent {
            IdeaPluginTheme {
                CompositionLocalProvider(
                    LocalProject provides project
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colors.background),
                    ) {
                        content()
                    }
                }
            }
        }
    }
}
