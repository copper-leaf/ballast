package com.copperleaf.ballast.examples.mainlist

import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.copperleaf.ballast.examples.mainlist.MainComposeUi
import com.copperleaf.ballast.examples.mainlist.MainEventHandler
import com.copperleaf.ballast.examples.util.LocalInjector
import com.copperleaf.ballast.examples.util.ballastViewModelFactory

object MainAndroidUi {

    @Composable
    fun AndroidContent() {
        val injector = LocalInjector.current
        val vm: MainViewModel = viewModel(
            factory = ballastViewModelFactory()
        )

        val uiState by vm.observeStates().collectAsState()

        LaunchedEffect(vm) {
            vm.attachEventHandler(this, MainEventHandler(injector.routerViewModel()))
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ballast Examples") },
                )
            },
            content = {
                MainComposeUi.Content(
                    uiState = uiState,
                ) { vm.trySend(it) }
            }
        )
    }

}
