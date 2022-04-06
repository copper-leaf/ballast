package com.copperleaf.ballast.debugger.idea.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import com.copperleaf.ballast.debugger.di.BallastDebuggerInjector
import com.copperleaf.ballast.debugger.di.LocalInjector
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Composable
fun IdeaPluginTheme(
    project: Project,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val primaryColor = Color(0xff_9e9e9e)
    val secondaryColor = Color(0xff_ffab00)

    val materialColors = if (darkTheme) {
        darkColors(primary = primaryColor, secondary = secondaryColor)
    } else {
        lightColors(primary = primaryColor, secondary = secondaryColor)
    }

    val swingColor = SwingColor()

    MaterialTheme(
        colors = materialColors.copy(
            background = swingColor.background,
            onBackground = swingColor.onBackground,
            surface = swingColor.background,
            onSurface = swingColor.onBackground,
        ),
        typography = typography,
        content = {
            CompositionLocalProvider(LocalInjector provides BallastDebuggerInjector.getInstance(project)) {
                content()
            }
        }
    )
}
