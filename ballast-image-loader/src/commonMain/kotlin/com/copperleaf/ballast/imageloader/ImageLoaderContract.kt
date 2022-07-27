package com.copperleaf.ballast.imageloader

import androidx.compose.ui.graphics.ImageBitmap

object ImageLoaderContract {
    data class State(
        val imagesInView: Map<ImageDescription, Int> = emptyMap(),
        val imageCache: Map<String, CachedImage> = emptyMap(),
    )

    sealed class Inputs {
        data class ImageEnteredView(val image: ImageDescription) : Inputs()
        data class ImageLeftView(val image: ImageDescription) : Inputs()
        data class ImageLoaded(val image: ImageDescription, val imageResult: Result<ImageBitmap>) : Inputs()
    }

    sealed class Events {

    }
}
