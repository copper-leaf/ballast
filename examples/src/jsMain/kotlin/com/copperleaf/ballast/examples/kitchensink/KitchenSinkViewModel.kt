package com.copperleaf.ballast.examples.kitchensink

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.BallastViewModel
import com.copperleaf.ballast.core.BasicViewModel
import kotlinx.coroutines.CoroutineScope

actual class KitchenSinkViewModel(
    viewModelCoroutineScope: CoroutineScope,
    config: BallastViewModelConfiguration<
        KitchenSinkContract.Inputs,
        KitchenSinkContract.Events,
        KitchenSinkContract.State>,
    eventHandler: KitchenSinkEventHandler,
) : BasicViewModel<
    KitchenSinkContract.Inputs,
    KitchenSinkContract.Events,
    KitchenSinkContract.State>(
    config = config,
    eventHandler = eventHandler,
    coroutineScope = viewModelCoroutineScope
), BallastViewModel<
    KitchenSinkContract.Inputs,
    KitchenSinkContract.Events,
    KitchenSinkContract.State>
