package com.copperleaf.ballast.examples.presentation.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class Colors(
    val backgroundColor: Color,
    val contentColor: Color,
) {
    companion object {
        val yellow = Colors(
            Color(253, 243, 216),
            Color(139, 108, 29),
        )
        val purple = Colors(
            Color(237, 223, 246),
            Color(110, 33, 186),
        )
        val green = Colors(
            Color(226, 248, 232),
            Color(24, 123, 52),
        )
        val red = Colors(
            Color(255, 220, 220),
            Color(169, 30, 30),
        )
        val blue = Colors(
            Color(221, 237, 253),
            Color(15, 88, 189),
        )
        val gray = Colors(
            Color(240, 240, 240),
            Color(102, 102, 102),
        )
        val pink = Colors(
            Color(248, 231, 243),
            Color(161, 43, 134),
        )
        val orange = Colors(
            Color(252, 227, 206),
            Color(196, 91, 28),
        )

        val surface
            @Composable
            get() = Colors(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.onSurface
            )
    }
}
