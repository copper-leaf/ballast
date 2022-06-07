package com.copperleaf.ballast.examples.util

import androidx.compose.runtime.compositionLocalOf

val LocalInjector = compositionLocalOf<ComposeDesktopInjector> { error("LocalInjector not provided") }
