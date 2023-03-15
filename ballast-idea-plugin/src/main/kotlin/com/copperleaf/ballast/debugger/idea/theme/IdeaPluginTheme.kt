package com.copperleaf.ballast.debugger.idea.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.copperleaf.ballast.debugger.idea.settings.IntellijPluginSettingsSnapshot
import com.copperleaf.ballast.debugger.idea.ui.debugger.ui.widgets.LocalProject
import com.copperleaf.ballast.repository.cache.Cached
import com.copperleaf.ballast.repository.cache.getCachedOrNull
import com.intellij.openapi.project.Project

@Composable
fun IdeaPluginTheme(
    project: Project,
    settings: Cached<IntellijPluginSettingsSnapshot>,
    content: @Composable () -> Unit,
) {
    settings.getCachedOrNull()?.let {
        IdeaPluginTheme(project, it, content)
    }
}

@Composable
private fun IdeaPluginTheme(
    project: Project,
    settings: IntellijPluginSettingsSnapshot,
    content: @Composable () -> Unit,
) {


    val primaryColor = Color(0xff_ffab00)
    val secondaryColor = Color(0xff_ffab00)
    val swingColors = SwingColor()

    val materialColors = if (settings.darkTheme) {
        darkColors(
            primary = primaryColor,
            secondary = secondaryColor,
            background = swingColors.background,
            surface = swingColors.background,

            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White,
            onError = Color.Black,
        )
    } else {
        lightColors(
            primary = primaryColor,
            secondary = secondaryColor,
            background = swingColors.background,
            surface = swingColors.background,

            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color.Black,
            onSurface = Color.Black,
            onError = Color.White
        )
    }

    MaterialTheme(
        colors = materialColors,
        typography = typography,
        shapes = shapes,
        content = {
//            DesktopTheme {
            CompositionLocalProvider(
                LocalProject provides project,
                LocalContentColor provides materialColors.onBackground,
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colors.background),
                ) {
                    content()
                }
            }
//            }
        }
    )
}
