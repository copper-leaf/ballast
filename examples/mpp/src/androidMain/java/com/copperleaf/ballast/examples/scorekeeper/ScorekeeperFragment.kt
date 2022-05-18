package com.copperleaf.ballast.examples.scorekeeper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.copperleaf.ballast.examples.util.BallastViewModelFactory

class ScorekeeperFragment : Fragment() {

    val vm: ScorekeeperViewModel by viewModels { BallastViewModelFactory(this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext())
            .apply {
                setContent {
                    val uiState by vm.observeStates().collectAsState()
                    val snackbarHostState = remember { SnackbarHostState() }

                    LaunchedEffect(vm, snackbarHostState) {
                        vm.attachEventHandler(this, ScorekeeperEventHandler { snackbarHostState.showSnackbar(it) })
                    }

                    ScorekeeperComposeUi.Content(snackbarHostState, uiState) { vm.trySend(it) }
                }
            }
    }
}
