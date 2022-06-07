package com.copperleaf.ballast.examples.scorekeeper

import com.copperleaf.ballast.examples.scorekeeper.models.Player

object ScorekeeperContract {
    data class State(
        val buttonValues: List<Int> = listOf(1, 5, 10),
        val players: List<Player> = emptyList(),
    )

    sealed class Inputs {
        data class AddPlayer(val playerName: String) : Inputs()
        data class RemovePlayer(val playerName: String) : Inputs()
        data class ChangeScore(val amount: Int) : Inputs()

        object CommitAllTempScores : Inputs()
        data class CommitTempScore(val playerName: String) : Inputs()

        data class TogglePlayerSelection(val playerName: String) : Inputs()

        object GoBack : Inputs()
    }

    sealed class Events {
        data class ShowErrorMessage(val text: String): Events()

        object NavigateBackwards : Events()
    }
}
