package com.copperleaf.ballast.examples.scheduler

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused") // Used in iOS
fun RootViewController(): UIViewController = ComposeUIViewController {
    SchedulerExampleUi.Content()
}
