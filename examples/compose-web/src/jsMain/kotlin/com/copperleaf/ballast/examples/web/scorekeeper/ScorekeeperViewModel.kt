package com.copperleaf.ballast.examples.web.scorekeeper

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperContract
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperEventHandler
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperInputHandler
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperSavedStateAdapter
import com.copperleaf.ballast.examples.scorekeeper.prefs.ScoreKeeperPrefs
import com.copperleaf.ballast.forViewModel
import com.copperleaf.ballast.plusAssign
import com.copperleaf.ballast.savedstate.BallastSavedStateInterceptor
import kotlinx.coroutines.CoroutineScope
import kotlin.time.ExperimentalTime

@ExperimentalTime
class ScorekeeperViewModel(
    viewModelCoroutineScope: CoroutineScope,
    prefs: ScoreKeeperPrefs,
    configurationBuilder: BallastViewModelConfiguration.Builder,
    displayErrorMessage: (String) -> Unit
) : BasicViewModel<
    ScorekeeperContract.Inputs,
    ScorekeeperContract.Events,
    ScorekeeperContract.State>(
    config = configurationBuilder
        .apply {
            this += BallastSavedStateInterceptor(
                ScorekeeperSavedStateAdapter(prefs)
            )
        }
        .forViewModel(
            initialState = ScorekeeperContract.State(),
            inputHandler = ScorekeeperInputHandler(),
            name = "Scorekeeper",
        ),
    eventHandler = ScorekeeperEventHandler(displayErrorMessage),
    coroutineScope = viewModelCoroutineScope,
)
