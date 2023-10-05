package com.copperleaf.ballast.examples.counter

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val Icons.Filled.Remove: ImageVector by lazy {
    ImageVector.Builder(
        name = "remove",
        defaultWidth = 40.0.dp,
        defaultHeight = 40.0.dp,
        viewportWidth = 40.0f,
        viewportHeight = 40.0f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(8.542f, 21.292f)
            verticalLineToRelative(-2.625f)
            horizontalLineToRelative(22.916f)
            verticalLineToRelative(2.625f)
            close()
        }
    }.build()
}

public val Icons.Filled.Add: ImageVector by lazy {
    ImageVector.Builder(
        name = "add",
        defaultWidth = 40.0.dp,
        defaultHeight = 40.0.dp,
        viewportWidth = 40.0f,
        viewportHeight = 40.0f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(18.708f, 31.458f)
            verticalLineTo(21.292f)
            horizontalLineTo(8.542f)
            verticalLineToRelative(-2.625f)
            horizontalLineToRelative(10.166f)
            verticalLineTo(8.542f)
            horizontalLineToRelative(2.625f)
            verticalLineToRelative(10.125f)
            horizontalLineToRelative(10.125f)
            verticalLineToRelative(2.625f)
            horizontalLineTo(21.333f)
            verticalLineToRelative(10.166f)
            close()
        }
    }.build()
}
