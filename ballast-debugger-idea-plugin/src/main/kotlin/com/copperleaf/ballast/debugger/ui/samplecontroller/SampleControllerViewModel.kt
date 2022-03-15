package com.copperleaf.ballast.debugger.ui.samplecontroller

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.forViewModel
import kotlinx.coroutines.CoroutineScope

class SampleControllerViewModel(
    coroutineScope: CoroutineScope,
    configurationBuilder: BallastViewModelConfiguration.Builder,
    inputHandler: SampleControllerInputHandler,
    eventHandler: SampleControllerEventHandler,
) : BasicViewModel<
    SampleControllerContract.Inputs,
    SampleControllerContract.Events,
    SampleControllerContract.State>(
    config = configurationBuilder
        .forViewModel(
            initialState = SampleControllerContract.State(),
            inputHandler = inputHandler,
            name = "Sample Controller",
        ),
    eventHandler = eventHandler,
    coroutineScope = coroutineScope
)
