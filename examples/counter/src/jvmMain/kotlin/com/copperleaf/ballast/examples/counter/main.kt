package com.copperleaf.ballast.examples.counter

import androidx.compose.ui.window.singleWindowApplication

fun main() = singleWindowApplication(title = "Ballast Examples > Counter") {
    CounterUi.Content()
}
