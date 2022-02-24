package com.copperleaf.ballast.debugger

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.application
import com.copperleaf.ballast.debugger.windows.debugger.DebuggerWindow
import com.copperleaf.ballast.debugger.windows.sample.SampleWindow

fun main() = application {
    var darkMode by remember { mutableStateOf(true) }
    var showSampleWindow by remember { mutableStateOf(false) }

    val primaryColor = Color(0xff_9e9e9e)
    val secondaryColor = Color(0xff_ffab00)

    val materialColors = if (darkMode) {
        darkColors(primary = primaryColor, secondary = secondaryColor)
    } else {
        lightColors(primary = primaryColor, secondary = secondaryColor)
    }

    MaterialTheme(colors = materialColors) {
        DebuggerWindow().run(this, darkMode, { darkMode = it }, showSampleWindow, { showSampleWindow = it })

        if (showSampleWindow) {
            SampleWindow().run(this, { showSampleWindow = it })
        }
    }
}
