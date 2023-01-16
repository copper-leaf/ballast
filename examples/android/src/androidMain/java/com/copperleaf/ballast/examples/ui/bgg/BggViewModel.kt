package com.copperleaf.ballast.examples.ui.bgg

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.AndroidViewModel
import kotlinx.coroutines.CoroutineScope

class BggViewModel(
    config: BallastViewModelConfiguration<
        BggContract.Inputs,
        BggContract.Events,
        BggContract.State>,
    coroutineScope: CoroutineScope,
) : AndroidViewModel<
    BggContract.Inputs,
    BggContract.Events,
    BggContract.State>(
    config = config,
    coroutineScope = coroutineScope,
)
