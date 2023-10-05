package com.copperleaf.ballast.examples.counter

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused") // Used in iOS
fun RootViewController(): UIViewController = ComposeUIViewController {
    CounterUi.Content()
}
