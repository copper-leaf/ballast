package com.copperleaf.ballast.examples.ui.kitchensink

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.AndroidViewModel
import kotlinx.coroutines.CoroutineScope

class KitchenSinkViewModel(
    config: BallastViewModelConfiguration<
        KitchenSinkContract.Inputs,
        KitchenSinkContract.Events,
        KitchenSinkContract.State>,
    coroutineScope: CoroutineScope,
) : AndroidViewModel<
    KitchenSinkContract.Inputs,
    KitchenSinkContract.Events,
    KitchenSinkContract.State>(
    config = config,
    coroutineScope = coroutineScope,
)
