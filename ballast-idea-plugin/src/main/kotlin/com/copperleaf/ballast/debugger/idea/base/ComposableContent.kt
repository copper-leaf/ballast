package com.copperleaf.ballast.debugger.idea.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.awt.ComposePanel
import com.copperleaf.ballast.debugger.di.BallastDebuggerInjector
import com.copperleaf.ballast.debugger.di.LocalInjector
import com.copperleaf.ballast.debugger.di.LocalProject
import com.copperleaf.ballast.debugger.idea.theme.IdeaPluginTheme
import com.intellij.openapi.project.Project

fun interface ComposableContent {
    @Composable
    fun Content()

    interface Provider {
        fun provideComposableContent(): ComposableContent

        fun getComposePanel(project: Project): ComposePanel {
            val composableContent = provideComposableContent()
            return ComposePanel().apply {
                setContent {
                    IdeaPluginTheme {
                        CompositionLocalProvider(
                            LocalProject provides project,
                            LocalInjector provides BallastDebuggerInjector.getInstance(project),
                        ) {
                            composableContent.Content()
                        }
                    }
                }
            }
        }
    }
}
