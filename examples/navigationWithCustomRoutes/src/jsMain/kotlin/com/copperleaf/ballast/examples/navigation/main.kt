package com.copperleaf.ballast.examples.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.CanvasBasedWindow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.skiko.wasm.onWasmReady
import kotlin.coroutines.resume

@OptIn(ExperimentalComposeUiApi::class)
public fun main() {
    MainScope().launch {
        suspendCancellableCoroutine<Unit> { cont ->
            onWasmReady {
                cont.resume(Unit)
            }
        }

        CanvasBasedWindow("Ballast Examples > Navigation with Custom Routes") {
            Box(Modifier.requiredWidth(400.dp)) {
                NavigationUi.Content()
            }
        }
    }
}
