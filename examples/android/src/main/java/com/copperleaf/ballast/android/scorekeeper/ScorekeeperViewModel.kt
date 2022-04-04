package com.copperleaf.ballast.android.scorekeeper

import com.copperleaf.ballast.android.util.ScorekeeperPrefsImpl
import com.copperleaf.ballast.android.util.commonBuilder
import com.copperleaf.ballast.core.AndroidViewModel
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperContract
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperInputHandler
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperSavedStateAdapter
import com.copperleaf.ballast.forViewModel
import com.copperleaf.ballast.plusAssign
import com.copperleaf.ballast.savedstate.BallastSavedStateInterceptor
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ScorekeeperViewModel : AndroidViewModel<
    ScorekeeperContract.Inputs,
    ScorekeeperContract.Events,
    ScorekeeperContract.State>(
    config = commonBuilder()
        .apply {
            this += BallastSavedStateInterceptor(
                ScorekeeperSavedStateAdapter(ScorekeeperPrefsImpl())
            )
        }
        .forViewModel(
            initialState = ScorekeeperContract.State(),
            inputHandler = ScorekeeperInputHandler(),
            name = "Scorekeeper",
        ),
)
