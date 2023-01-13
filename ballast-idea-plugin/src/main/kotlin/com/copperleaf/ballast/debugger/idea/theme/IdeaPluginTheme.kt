package com.copperleaf.ballast.debugger.idea.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun IdeaPluginTheme(
    darkTheme: Boolean = true,
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
            content()
        }
    )
}
