package com.ballast.sharedui

import androidx.compose.ui.window.Application
import com.ballast.sharedui.root.RootContent
import platform.UIKit.UIViewController


@Suppress("FunctionName", "unused") // Used in iOS
fun RootViewController(): UIViewController = Application("Ballast Shared UI") {
    RootContent()
}
