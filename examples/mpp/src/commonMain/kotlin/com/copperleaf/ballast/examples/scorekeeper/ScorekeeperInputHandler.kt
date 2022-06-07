package com.copperleaf.ballast.examples.scorekeeper

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope
import com.copperleaf.ballast.examples.scorekeeper.models.Player
import kotlinx.coroutines.delay

class ScorekeeperInputHandler : InputHandler<
    ScorekeeperContract.Inputs,
    ScorekeeperContract.Events,
    ScorekeeperContract.State> {
    override suspend fun InputHandlerScope<
        ScorekeeperContract.Inputs,
        ScorekeeperContract.Events,
        ScorekeeperContract.State>.handleInput(
        input: ScorekeeperContract.Inputs
    ) = when (input) {
        is ScorekeeperContract.Inputs.AddPlayer -> {
            if (input.playerName.isBlank()) {
                postEvent(ScorekeeperContract.Events.ShowErrorMessage("Player names cannot be empty"))
            } else if (getCurrentState().players.any { it.name == input.playerName }) {
                postEvent(ScorekeeperContract.Events.ShowErrorMessage("Player with name '${input.playerName}' already exists"))
            } else {
                updateState {
                    it.copy(
                        players = it.players + Player(
                            name = input.playerName,
                        )
                    )
                }
            }
        }
        is ScorekeeperContract.Inputs.RemovePlayer -> {
            updateState {
                it.copy(
                    players = it.players
                        .filterNot { player ->
                            player.name == input.playerName
                        }
                )
            }
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
            updateState {
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
        }
        is ScorekeeperContract.Inputs.CommitAllTempScores -> {
            updateState {
                it.copy(
                    players = it.players.map { player ->
                        player.commitScore()
                    }
                )
            }
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

        is ScorekeeperContract.Inputs.GoBack -> {
            postEvent(ScorekeeperContract.Events.NavigateBackwards)
        }
    }
}
