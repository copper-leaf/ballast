package com.copperleaf.ballast.examples.scheduler

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
public fun main() {
    CanvasBasedWindow("Ballast Examples > Scheduler") {
        Box(Modifier.requiredWidth(400.dp)) {
            SchedulerExampleUi.Content()
        }
    }
}
