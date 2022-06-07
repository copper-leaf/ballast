package com.copperleaf.ballast.examples.bgg

import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.copperleaf.ballast.examples.bgg.models.HotListType
import com.copperleaf.ballast.examples.bgg.ui.BggContract
import com.copperleaf.ballast.examples.util.LocalInjector

object BggDesktopUi  {
    @Composable
    fun DesktopContent() {
        val injector = LocalInjector.current

        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope) { injector.bggViewModel(viewModelCoroutineScope) }
        val uiState by vm.observeStates().collectAsState()

        var dropdownExpanded by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { vm.trySend(BggContract.Inputs.GoBack) }) {
                            Icon(Icons.Default.ArrowBack, "")
                        }
                    },
                    title = { Text("BGG") },
                )
            },
            content = {
                BggComposeUi.Content(
                    openDropdown = { dropdownExpanded = true },
                    dropdownContainer = {
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                        ) {
                            HotListType.values().forEach {
                                DropdownMenuItem(
                                    onClick = {
                                        vm.trySend(BggContract.Inputs.ChangeHotListType(it))
                                        dropdownExpanded = false
                                    },
                                ) {
                                    Text(it.displayName)
                                }
                            }
                        }
                    },
                    uiState = uiState,
                ) { vm.trySend(it) }
            }
        )
    }
}
