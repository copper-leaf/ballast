package com.copperleaf.ballast.examples.kitchensink

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.copperleaf.ballast.examples.kitchensink.controller.InputStrategySelection
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerContract
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerEventHandler
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerUi
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerViewModel
import com.copperleaf.ballast.examples.util.LocalInjector
import com.copperleaf.ballast.examples.util.ballastViewModelFactory

object KitchenSinkAndroidUi {

    @Composable
    fun AndroidContent() {
        val injector = LocalInjector.current
        val vm: KitchenSinkControllerViewModel = viewModel(
            factory = ballastViewModelFactory()
        )

        val uiState by vm.observeStates().collectAsState()

        LaunchedEffect(vm) {
            vm.attachEventHandler(this, KitchenSinkControllerEventHandler(injector.routerViewModel()))
        }

        var dropdownExpanded by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { vm.trySend(KitchenSinkControllerContract.Inputs.GoBack) }) {
                            Icon(Icons.Default.ArrowBack, "")
                        }
                    },
                    title = { Text("Kitchen Sink") },
                )
            },
            content = {
                KitchenSinkControllerUi.Content(
                    openDropdown = { dropdownExpanded = true },
                    dropdownContainer = {
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            InputStrategySelection.values().forEach { strategy ->
                                DropdownMenuItem(
                                    onClick = {
                                        dropdownExpanded = false
                                        vm.trySend(
                                            KitchenSinkControllerContract.Inputs.UpdateInputStrategy(strategy)
                                        )
                                    },
                                    content = { Text(strategy.name) },
                                )
                            }
                        }
                    },
                    uiState = uiState,
                ) { vm.trySend(it) }
            }
        )
    }

}
