@file:Suppress("UNUSED_PARAMETER")

package com.copperleaf.ballast.debugger.idea.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposePanel
import com.intellij.openapi.wm.ToolWindow
import javax.swing.JComponent

fun ToolWindow.setContent(
    displayName: String?,
    isLockable: Boolean = true,
    content: @Composable () -> Unit
) = BallastComposePanel(content = content)
    .also { contentManager.addContent(contentManager.factory.createContent(it, displayName, isLockable)) }

fun BallastComposePanel(
    height: Int = 800,
    width: Int = 800,
    y: Int = 0,
    x: Int = 0,
    content: @Composable () -> Unit
): JComponent {
    return ComposePanel().apply {
        setContent {
            content()
        }
    }
}
