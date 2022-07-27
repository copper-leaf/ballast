package com.copperleaf.ballast.imageloader

import androidx.compose.ui.graphics.ImageBitmap

data class CachedImage(
    val description: ImageDescription,
    val image: Result<ImageBitmap>,
)
