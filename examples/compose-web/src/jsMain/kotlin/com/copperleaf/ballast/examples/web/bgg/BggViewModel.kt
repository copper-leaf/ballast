package com.copperleaf.ballast.examples.web.bgg

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.examples.bgg.repository.BggRepository
import com.copperleaf.ballast.forViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.copperleaf.ballast.examples.bgg.ui.BggContract
import com.copperleaf.ballast.examples.bgg.ui.BggInputHandler
import com.copperleaf.ballast.examples.bgg.ui.BggEventHandler

@ExperimentalCoroutinesApi
class BggViewModel(
    viewModelCoroutineScope: CoroutineScope,
    configurationBuilder: BallastViewModelConfiguration.Builder,
    repository: BggRepository,
) : BasicViewModel<
    BggContract.Inputs,
    BggContract.Events,
    BggContract.State>(
    config = configurationBuilder
        .forViewModel(
            initialState = BggContract.State(),
            inputHandler = BggInputHandler(repository),
            name = "BGG",
        ),
    eventHandler = BggEventHandler(),
    coroutineScope = viewModelCoroutineScope,
)
