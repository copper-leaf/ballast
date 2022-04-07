package com.copperleaf.ballast.examples.bgg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.copperleaf.ballast.examples.util.BallastViewModelFactory
import com.copperleaf.ballast.examples.bgg.BggComposeUi
import com.copperleaf.ballast.examples.bgg.models.HotListType
import com.copperleaf.ballast.examples.bgg.ui.BggContract
import com.copperleaf.ballast.examples.bgg.ui.BggEventHandler

class BggFragment : Fragment() {

    val eventHandler = BggEventHandler()
    val vm: BggViewModel by viewModels { BallastViewModelFactory(this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext())
            .apply {
                setContent {
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vm.attachEventHandler(this, eventHandler)
    }
}
