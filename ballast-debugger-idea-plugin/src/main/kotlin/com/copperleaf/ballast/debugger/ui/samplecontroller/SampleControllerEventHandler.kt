package com.copperleaf.ballast.debugger.ui.samplecontroller

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.diagnostic.Logger

class SampleControllerEventHandler(
    private val logger: Logger,
) : EventHandler<
        SampleControllerContract.Inputs,
        SampleControllerContract.Events,
        SampleControllerContract.State> {
    override suspend fun EventHandlerScope<
        SampleControllerContract.Inputs,
        SampleControllerContract.Events,
        SampleControllerContract.State>.handleEvent(
        event: SampleControllerContract.Events
    ) = when (event) {
        is SampleControllerContract.Events.OpenUrlInBrowser -> {
            BrowserUtil.browse(event.url)
        }
    }
}
