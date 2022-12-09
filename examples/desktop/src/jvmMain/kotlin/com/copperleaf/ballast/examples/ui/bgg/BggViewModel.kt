package com.copperleaf.ballast.examples.ui.bgg

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import kotlinx.coroutines.CoroutineScope

class BggViewModel(
    viewModelCoroutineScope: CoroutineScope,
    config: BallastViewModelConfiguration<
        BggContract.Inputs,
        BggContract.Events,
        BggContract.State>,
    eventHandler: BggEventHandler,
) : BasicViewModel<
    BggContract.Inputs,
    BggContract.Events,
    BggContract.State>(
    config = config,
    eventHandler = eventHandler,
    coroutineScope = viewModelCoroutineScope,
)
