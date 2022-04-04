package com.copperleaf.ballast.examples.scorekeeper

import com.copperleaf.ballast.examples.scorekeeper.models.Player
import com.copperleaf.ballast.examples.scorekeeper.prefs.ScoreKeeperPrefs
import com.copperleaf.ballast.savedstate.RestoreStateScope
import com.copperleaf.ballast.savedstate.SaveStateScope
import com.copperleaf.ballast.savedstate.SavedStateAdapter

class ScorekeeperSavedStateAdapter(
    private val prefs: ScoreKeeperPrefs,
) : SavedStateAdapter<
    ScorekeeperContract.Inputs,
    ScorekeeperContract.Events,
    ScorekeeperContract.State> {

    override suspend fun SaveStateScope<ScorekeeperContract.Inputs, ScorekeeperContract.Events, ScorekeeperContract.State>.save() {
        saveDiff({ this.players }) { players ->
            prefs.scoresheetState = players.map { it.name to it.score }.toMap()
        }
    }

    override suspend fun RestoreStateScope<ScorekeeperContract.Inputs, ScorekeeperContract.Events, ScorekeeperContract.State>.restore(): ScorekeeperContract.State {
        val playerList = prefs
            .scoresheetState
            .entries
            .map {
                Player(
                    name = it.key,
                    score = it.value,
                )
            }

        return ScorekeeperContract.State(
            players = playerList
        )
    }
}
