package com.copperleaf.ballast.debugger.ui.kitchensink

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkContract
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkEventHandler
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkInputHandler
import com.copperleaf.ballast.forViewModel
import kotlinx.coroutines.CoroutineScope

class KitchenSinkViewModel(
    viewModelCoroutineScope: CoroutineScope,
    configurationBuilder: BallastViewModelConfiguration.Builder,
    onWindowClosed: ()->Unit,
) : BasicViewModel<
    KitchenSinkContract.Inputs,
    KitchenSinkContract.Events,
    KitchenSinkContract.State>(
    config = configurationBuilder
        .forViewModel(
            initialState = KitchenSinkContract.State(),
            inputHandler = KitchenSinkInputHandler(),
            name = "Kitchen Sink",
        ),
    eventHandler = KitchenSinkEventHandler(onWindowClosed),
    coroutineScope = viewModelCoroutineScope
)