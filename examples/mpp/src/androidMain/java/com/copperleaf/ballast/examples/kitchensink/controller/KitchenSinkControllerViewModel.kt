package com.copperleaf.ballast.examples.kitchensink.controller

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.AndroidViewModel
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerContract

class KitchenSinkControllerViewModel(
    config: BallastViewModelConfiguration<
        KitchenSinkControllerContract.Inputs,
        KitchenSinkControllerContract.Events,
        KitchenSinkControllerContract.State>,
) : AndroidViewModel<
    KitchenSinkControllerContract.Inputs,
    KitchenSinkControllerContract.Events,
    KitchenSinkControllerContract.State>(
    config = config,
)
