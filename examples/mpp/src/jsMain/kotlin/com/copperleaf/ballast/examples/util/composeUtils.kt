package com.copperleaf.ballast.examples.util

import androidx.compose.runtime.compositionLocalOf

val LocalInjector = compositionLocalOf<ComposeWebInjector> { error("LocalInjector not provided") }
