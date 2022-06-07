package com.copperleaf.ballast.examples.scorekeeper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.examples.scorekeeper.models.Player
import com.copperleaf.ballast.examples.util.ExamplesContext
import com.copperleaf.ballast.examples.util.LocalInjector
import com.copperleaf.ballast.examples.util.bulma.BulmaButton
import com.copperleaf.ballast.examples.util.bulma.BulmaButtonFeatures
import com.copperleaf.ballast.examples.util.bulma.BulmaButtonGroup
import com.copperleaf.ballast.examples.util.bulma.BulmaColor
import com.copperleaf.ballast.examples.util.bulma.BulmaInput
import com.copperleaf.ballast.examples.util.bulma.BulmaPanel
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Text

object ScorekeeperWebUi {
    @Composable
    public fun WebContent() {
        val injector = LocalInjector.current

        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope) { injector.scorekeeperViewModel(viewModelCoroutineScope) }
        val uiState by vm.observeStates().collectAsState()

        BulmaPanel(
            headingStart = { Text("Scorekeeper") },
            headingEnd = {
                A(
                    href = "${ExamplesContext.samplesUrlWithVersion}/scorekeeper",
                    attrs = {
                        title("View sources on GitHub")
                        target(ATarget.Blank)
                    }
                ) {
                    I { Text("Sources") }
                }
            },
        ) {
            NewPlayerForm(uiState, vm::trySend)
            PlayersList(uiState, vm::trySend)
            Buttons(uiState, vm::trySend)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @Composable
    private fun NewPlayerForm(
        uiState: ScorekeeperContract.State,
        postInput: (ScorekeeperContract.Inputs) -> Unit,
    ) {
        val (playerName, setPlayerName) = remember { mutableStateOf("") }
        BulmaInput(
            "New Player Name",
            playerName,
            setPlayerName
        )
        BulmaButton({
            postInput(ScorekeeperContract.Inputs.AddPlayer(playerName))
            setPlayerName("")
        }) {
            Text("Add")
        }
        Hr { }
    }

    @Composable
    private fun PlayersList(
        uiState: ScorekeeperContract.State,
        postInput: (ScorekeeperContract.Inputs) -> Unit,
    ) {
        uiState
            .players
            .forEach {
                PlayerCard(it, postInput)
            }
    }

    @Composable
    private fun PlayerCard(
        player: Player,
        postInput: (ScorekeeperContract.Inputs) -> Unit,
    ) {
        BulmaPanel(
            headingStart = { Text(player.name) },
            headingEnd = {
                BulmaButton(
                    onClick = { postInput(ScorekeeperContract.Inputs.RemovePlayer(player.name)) },
                    color = BulmaColor.Danger,
                    features = setOf(BulmaButtonFeatures.Outlined)
                ) { Text("X") }
            },
            color = if (player.selected) BulmaColor.Primary else BulmaColor.Default,
            onClick = { postInput(ScorekeeperContract.Inputs.TogglePlayerSelection(player.name)) }
        ) {
            BulmaButtonGroup {
                Control {
                    BulmaButton(
                        onClick = { postInput(ScorekeeperContract.Inputs.CommitTempScore(player.name)) },
                        color = if (player.tempScore != 0) BulmaColor.Warning else BulmaColor.Ghost,
                    ) {
                        Text(player.scoreDisplay)
                    }
                }
            }
        }
    }

    @Composable
    private fun Buttons(
        uiState: ScorekeeperContract.State,
        postInput: (ScorekeeperContract.Inputs) -> Unit,
    ) {
        BulmaButtonGroup {
            uiState.buttonValues.forEach { buttonValue ->
                Control {
                    BulmaButton(
                        onClick = { postInput(ScorekeeperContract.Inputs.ChangeScore(buttonValue)) },
                        color = BulmaColor.Success,
                    ) {
                        Text("+$buttonValue")
                    }
                }
            }
        }
        BulmaButtonGroup {
            uiState.buttonValues.forEach { buttonValue ->
                Control {
                    BulmaButton(
                        onClick = { postInput(ScorekeeperContract.Inputs.ChangeScore(-1 * buttonValue)) },
                        color = BulmaColor.Danger,
                    ) {
                        Text("-$buttonValue")
                    }
                }
            }
        }
    }
}
