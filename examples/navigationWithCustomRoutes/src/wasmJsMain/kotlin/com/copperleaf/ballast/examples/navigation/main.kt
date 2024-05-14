package com.copperleaf.ballast.examples.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
public fun main() {
    CanvasBasedWindow("Ballast Examples > Navigation with Custom Routes") {
        Box(Modifier.requiredWidth(400.dp)) {
            NavigationUi.Content()
        }
    }
}
