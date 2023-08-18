package com.copperleaf.ballast.examples.ui.scorekeeper

import com.copperleaf.ballast.examples.ui.scorekeeper.models.Player


object ScorekeeperContract {
    data class State(
        val buttonValues: List<Int> = listOf(1, 5, 10),
        val players: List<Player> = emptyList(),
    )

    sealed class Inputs {
        data object GoBack : Inputs()

        data class AddPlayer(val playerName: String) : Inputs()
        data class RemovePlayer(val playerName: String) : Inputs()
        data class ChangeScore(val amount: Int) : Inputs()

        data object CommitAllTempScores : Inputs()
        data class CommitTempScore(val playerName: String) : Inputs()

        data class TogglePlayerSelection(val playerName: String) : Inputs()
    }

    sealed class Events {
        data object GoBack : Events()
        data class ShowErrorMessage(val text: String): Events()
    }
}
