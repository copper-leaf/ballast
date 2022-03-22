package com.copperleaf.ballast.examples.scorekeeper

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.examples.scorekeeper.models.Player
import com.copperleaf.ballast.examples.scorekeeper.prefs.ScoreKeeperPrefs
import kotlinx.coroutines.delay

class ScorekeeperInputHandler(
    private val prefs: ScoreKeeperPrefs,
) : InputHandler<
    ScorekeeperContract.Inputs,
    ScorekeeperContract.Events,
    ScorekeeperContract.State> {
    override suspend fun InputHandlerScope<
        ScorekeeperContract.Inputs,
        ScorekeeperContract.Events,
        ScorekeeperContract.State>.handleInput(
        input: ScorekeeperContract.Inputs
    ) = when (input) {
        is ScorekeeperContract.Inputs.Initialize -> {
            val savedScores = prefs.scoresheetState
            val playerList = savedScores
                .entries
                .map {
                    Player(
                        name = it.key,
                        score = it.value,
                    )
                }

            updateState { it.copy(players = playerList) }
        }

        is ScorekeeperContract.Inputs.AddPlayer -> {
            if (input.playerName.isBlank()) {
                postEvent(ScorekeeperContract.Events.ShowErrorMessage("Player names cannot be empty"))
            } else if (getCurrentState().players.any { it.name == input.playerName }) {
                postEvent(ScorekeeperContract.Events.ShowErrorMessage("Player with name '${input.playerName}' already exists"))
            } else {
                val currentState = updateStateAndGet {
                    it.copy(
                        players = it.players + Player(
                            name = input.playerName,
                        )
                    )
                }
                savePlayerScores(currentState)
            }
        }
        is ScorekeeperContract.Inputs.RemovePlayer -> {
            val currentState = updateStateAndGet {
                it.copy(
                    players = it.players
                        .filterNot { player ->
                            player.name == input.playerName
                        }
                )
            }

            savePlayerScores(currentState)
        }

        is ScorekeeperContract.Inputs.ChangeScore -> {
            updateState {
                it.copy(
                    players = it.players.map { player ->
                        if (player.selected) {
                            player.copy(
                                tempScore = player.tempScore + input.amount,
                            )
                        } else {
                            player
                        }
                    }
                )
            }

            sideJob("ChangeScore") {
                delay(5000)
                postInput(ScorekeeperContract.Inputs.CommitAllTempScores)
            }
        }
        is ScorekeeperContract.Inputs.CommitTempScore -> {
            val currentState = updateStateAndGet {
                it.copy(
                    players = it.players.map { player ->
                        if (player.name == input.playerName) {
                            player.commitScore()
                        } else {
                            player
                        }
                    }
                )
            }

            savePlayerScores(currentState)
        }
        is ScorekeeperContract.Inputs.CommitAllTempScores -> {
            val currentState = updateStateAndGet {
                it.copy(
                    players = it.players.map { player ->
                        player.commitScore()
                    }
                )
            }

            savePlayerScores(currentState)
        }

        is ScorekeeperContract.Inputs.TogglePlayerSelection -> {
            updateState {
                it.copy(
                    players = it.players.map { player ->
                        if (player.name == input.playerName) {
                            player.copy(
                                selected = !player.selected,
                            )
                        } else {
                            player
                        }
                    }
                )
            }
        }
    }

    private fun savePlayerScores(currentState: ScorekeeperContract.State) {
        prefs.scoresheetState = currentState
            .players
            .map {
                it.name to it.score
            }
            .toMap()
    }
}
