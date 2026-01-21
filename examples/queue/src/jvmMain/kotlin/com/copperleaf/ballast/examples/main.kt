package com.copperleaf.ballast.examples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.copperleaf.ballast.examples.di.ComposeDesktopInjector
import com.copperleaf.ballast.examples.di.ComposeDesktopInjectorImpl
import com.copperleaf.ballast.examples.presentation.ui.MainScreenUi

fun main() = singleWindowApplication(
    title = "Ballast Examples",
    state = WindowState(WindowPlacement.Maximized)
) {
    val applicationCoroutineScope = rememberCoroutineScope()

    // Setup the injector, which will run the queue in the background
    val injector: ComposeDesktopInjector = remember(applicationCoroutineScope) {
        ComposeDesktopInjectorImpl(applicationCoroutineScope)
    }

    // setup UI to observe and interact with the queue
    MaterialTheme {
        Box(Modifier.fillMaxSize()) {
            MainScreenUi.Content(injector)

            SnackbarHost(injector.snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}
