package com.copperleaf.ballast.debugger.ui.samplecontroller

import com.copperleaf.ballast.core.BaseViewModel
import com.copperleaf.ballast.core.DefaultViewModelConfiguration
import kotlinx.coroutines.CoroutineScope

class SampleControllerViewModel(
    coroutineScope: CoroutineScope,
    inputHandler: SampleControllerInputHandler,
    eventHandler: SampleControllerEventHandler,
) : BaseViewModel<
    SampleControllerContract.Inputs,
    SampleControllerContract.Events,
    SampleControllerContract.State>(
    config = DefaultViewModelConfiguration(
        initialState = SampleControllerContract.State(),
        inputHandler = inputHandler,
        name = "Sample Controller",
    ),
    eventHandler = eventHandler,
    coroutineScope = coroutineScope
)
