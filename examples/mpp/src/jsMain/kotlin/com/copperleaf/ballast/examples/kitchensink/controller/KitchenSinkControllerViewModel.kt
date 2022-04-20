package com.copperleaf.ballast.examples.kitchensink.controller

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.eventHandler
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerContract
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerEventHandler
import kotlinx.coroutines.CoroutineScope

class KitchenSinkControllerViewModel(
    viewModelCoroutineScope: CoroutineScope,
    config: BallastViewModelConfiguration<
        KitchenSinkControllerContract.Inputs,
        KitchenSinkControllerContract.Events,
        KitchenSinkControllerContract.State>,
    eventHandler: KitchenSinkControllerEventHandler,
) : BasicViewModel<
    KitchenSinkControllerContract.Inputs,
    KitchenSinkControllerContract.Events,
    KitchenSinkControllerContract.State>(
    config = config,
    eventHandler = eventHandler,
    coroutineScope = viewModelCoroutineScope
)
