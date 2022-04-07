package com.copperleaf.ballast.examples.bgg

import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.copperleaf.ballast.examples.bgg.models.HotListType
import com.copperleaf.ballast.examples.bgg.ui.BggContract
import com.copperleaf.ballast.examples.util.Component
import com.copperleaf.ballast.examples.util.ComposeDesktopInjector

class BggComponent(
    private val injector: ComposeDesktopInjector
) : Component {
    @Composable
    override fun Content() {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope) { injector.bggViewModel(viewModelCoroutineScope) }
        val uiState by vm.observeStates().collectAsState()

        var dropdownExpanded by remember { mutableStateOf(false) }

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
}
