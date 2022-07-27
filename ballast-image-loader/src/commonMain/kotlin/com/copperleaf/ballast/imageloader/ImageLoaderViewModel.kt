package com.copperleaf.ballast.imageloader

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.forViewModel
import kotlinx.coroutines.CoroutineScope

class ImageLoaderViewModel(
    coroutineScope: CoroutineScope,
    configBuilder: BallastViewModelConfiguration.Builder,
    inputHandler: ImageLoaderInputHandler,
    eventHandler: ImageLoaderEventHandler,
) : BasicViewModel<
    ImageLoaderContract.Inputs,
    ImageLoaderContract.Events,
    ImageLoaderContract.State>(
    coroutineScope = coroutineScope,
    config = configBuilder
        .forViewModel(
            inputHandler = inputHandler,
            initialState = ImageLoaderContract.State(),
            name = "ImageLoader",
        ),
    eventHandler = eventHandler,
)
