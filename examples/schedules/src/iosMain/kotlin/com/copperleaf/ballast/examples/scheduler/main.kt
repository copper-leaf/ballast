package com.copperleaf.ballast.examples.scheduler

import androidx.compose.ui.window.ComposeUIViewController
import com.copperleaf.ballast.examples.scheduler.layout.SchedulerExampleLayout
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused") // Used in iOS
fun RootViewController(): UIViewController = ComposeUIViewController {
    SchedulerExampleLayout.Content()
}
