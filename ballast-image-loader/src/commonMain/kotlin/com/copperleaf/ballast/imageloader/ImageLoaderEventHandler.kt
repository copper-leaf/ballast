package com.copperleaf.ballast.imageloader

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope

class ImageLoaderEventHandler : EventHandler<
    ImageLoaderContract.Inputs,
    ImageLoaderContract.Events,
    ImageLoaderContract.State> {
    override suspend fun EventHandlerScope<
        ImageLoaderContract.Inputs,
        ImageLoaderContract.Events,
        ImageLoaderContract.State>.handleEvent(
        event: ImageLoaderContract.Events
    ) { }
}
