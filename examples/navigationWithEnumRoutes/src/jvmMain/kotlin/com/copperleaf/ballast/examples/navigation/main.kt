package com.copperleaf.ballast.examples.navigation

import androidx.compose.ui.window.singleWindowApplication

fun main() = singleWindowApplication(title = "Ballast Examples > Navigation With Enum Routes") {
    NavigationUi.Content()
}
