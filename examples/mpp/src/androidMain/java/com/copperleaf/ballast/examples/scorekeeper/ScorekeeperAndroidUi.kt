package com.copperleaf.ballast.examples.scorekeeper

import androidx.activity.compose.BackHandler
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.copperleaf.ballast.examples.util.LocalInjector
import com.copperleaf.ballast.examples.util.ballastViewModelFactory

object ScorekeeperAndroidUi {

    @Composable
    fun AndroidContent() {
        val injector = LocalInjector.current
        val vm: ScorekeeperViewModel = viewModel(
            factory = ballastViewModelFactory()
        )

        val uiState by vm.observeStates().collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(vm) {
            vm.attachEventHandler(this, ScorekeeperEventHandler(injector.routerViewModel()) { snackbarHostState.showSnackbar(it) } )
        }

        BackHandler { vm.trySend(ScorekeeperContract.Inputs.GoBack) }

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { vm.trySend(ScorekeeperContract.Inputs.GoBack) }) {
                            Icon(Icons.Default.ArrowBack, "")
                        }
                    },
                    title = { Text("Scorekeeper") },
                )
            },
            content = {
                ScorekeeperComposeUi.Content(
                    snackbarHostState = snackbarHostState,
                    uiState = uiState,
                ) { vm.trySend(it) }
            }
        )
    }

}
