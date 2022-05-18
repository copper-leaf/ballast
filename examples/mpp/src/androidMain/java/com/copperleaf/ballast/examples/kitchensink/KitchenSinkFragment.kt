package com.copperleaf.ballast.examples.kitchensink

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.copperleaf.ballast.examples.kitchensink.controller.InputStrategySelection
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerContract
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerEventHandler
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerUi
import com.copperleaf.ballast.examples.kitchensink.controller.KitchenSinkControllerViewModel
import com.copperleaf.ballast.examples.util.BallastViewModelFactory

class KitchenSinkFragment : Fragment() {

    val vm: KitchenSinkControllerViewModel by viewModels { BallastViewModelFactory(this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext())
            .apply {
                setContent {
                    val uiState by vm.observeStates().collectAsState()

                    LaunchedEffect(vm) {
                        vm.attachEventHandler(this, KitchenSinkControllerEventHandler())
                    }

                    var dropdownExpanded by remember { mutableStateOf(false) }

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
            }
    }
}
