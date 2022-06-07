package com.copperleaf.ballast.examples.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner

val LocalInjector = compositionLocalOf<AndroidInjector> { error("LocalInjector not provided") }

@Composable
fun ballastViewModelFactory(): BallastViewModelFactory {
    val injector = LocalInjector.current
    val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
    return BallastViewModelFactory(savedStateRegistryOwner, injector)
}
