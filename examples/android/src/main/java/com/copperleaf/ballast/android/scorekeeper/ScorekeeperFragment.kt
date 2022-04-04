package com.copperleaf.ballast.android.scorekeeper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperContract
import com.copperleaf.ballast.examples.scorekeeper.ScorekeeperEventHandler
import com.copperleaf.ballast.examples.scorekeeper.models.Player

@OptIn(ExperimentalMaterialApi::class)
class ScorekeeperFragment : Fragment() {

    val snackbarHostState = SnackbarHostState()
    val eventHandler = ScorekeeperEventHandler { snackbarHostState.showSnackbar(it) }
    val vm: ScorekeeperViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext())
            .apply {
                setContent {
                    val uiState by vm.observeStates().collectAsState()

                    Box(Modifier.fillMaxSize()) {
                        Content(uiState) { vm.trySend(it) }
                        SnackbarHost(
                            snackbarHostState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        )
                    }
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vm.attachEventHandler(this, eventHandler)
    }

    @Composable
    private fun Content(
        uiState: ScorekeeperContract.State,
        postInput: (ScorekeeperContract.Inputs) -> Unit,
    ) {
        Column {
            Column(Modifier.padding(all = 16.dp)) {
                NewPlayerForm(uiState, postInput)
                PlayersList(uiState, postInput)
            }
            Spacer(Modifier.weight(1f))
            Buttons(uiState, postInput)
        }
    }

    @Composable
    private fun NewPlayerForm(
        uiState: ScorekeeperContract.State,
        postInput: (ScorekeeperContract.Inputs) -> Unit,
    ) {
        val (newPlayerName, setNewPlayerName) = remember { mutableStateOf(TextFieldValue()) }
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = newPlayerName,
            onValueChange = setNewPlayerName,
            placeholder = { Text("Add Player") },
            trailingIcon = {
                IconButton(onClick = {
                    postInput(ScorekeeperContract.Inputs.AddPlayer(newPlayerName.text))
                    setNewPlayerName(TextFieldValue())
                }) {
                    Icon(Icons.Default.AddCircle, "Add Player")
                }
            }
        )
    }

    @Composable
    private fun PlayersList(
        uiState: ScorekeeperContract.State,
        postInput: (ScorekeeperContract.Inputs) -> Unit,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.players) { player ->
                PlayerCard(player, postInput)
            }
        }
    }

    @Composable
    private fun PlayerCard(
        player: Player,
        postInput: (ScorekeeperContract.Inputs) -> Unit,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { postInput(ScorekeeperContract.Inputs.TogglePlayerSelection(player.name)) },
            elevation = if (player.selected) 8.dp else 4.dp,
            border = if (player.selected) BorderStroke(1.dp, MaterialTheme.colors.primary) else null
        ) {
            Surface(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                Column {
                    ListItem(
                        text = { Text(player.name) },
                        trailing = {
                            IconButton(
                                onClick = { postInput(ScorekeeperContract.Inputs.RemovePlayer(player.name)) }
                            ) {
                                Icon(Icons.Default.Delete, "Remove ${player.name}")
                            }
                        }
                    )
                    Divider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { postInput(ScorekeeperContract.Inputs.CommitTempScore(player.name)) },
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(player.scoreDisplay, style = MaterialTheme.typography.h3)
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
        BottomNavigation {
            uiState.buttonValues.forEach { buttonValue ->
                BottomNavigationItem(
                    selected = false,
                    onClick = { postInput(ScorekeeperContract.Inputs.ChangeScore(buttonValue)) },
                    icon = { Text("+$buttonValue") }
                )
            }
        }

        BottomNavigation {
            uiState.buttonValues.forEach { buttonValue ->
                BottomNavigationItem(
                    selected = false,
                    onClick = { postInput(ScorekeeperContract.Inputs.ChangeScore(-1 * buttonValue)) },
                    icon = { Text("-$buttonValue") }
                )
            }
        }
    }
}
