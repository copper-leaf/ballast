package com.copperleaf.ballast.examples.scheduler

import androidx.compose.ui.window.singleWindowApplication
import com.copperleaf.ballast.examples.scheduler.layout.SchedulerExampleLayout

fun main() = singleWindowApplication(title = "Ballast Examples > Scheduler") {
    SchedulerExampleLayout.Content()
}
