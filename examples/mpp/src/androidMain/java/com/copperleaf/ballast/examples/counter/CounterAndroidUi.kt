package com.copperleaf.ballast.examples.counter

import android.view.LayoutInflater
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.copperleaf.ballast.examples.databinding.FragmentCounterBinding
import com.copperleaf.ballast.examples.util.LocalInjector
import com.copperleaf.ballast.examples.util.ballastViewModelFactory

object CounterAndroidUi {

    @Composable
    fun AndroidContent() {
        val injector = LocalInjector.current
        val vm: CounterViewModel = viewModel(
            factory = ballastViewModelFactory()
        )

        val uiState by vm.observeStates().collectAsState()

        LaunchedEffect(vm) {
            vm.attachEventHandler(this, CounterEventHandler(injector.routerViewModel()))
        }

        BackHandler { vm.trySend(CounterContract.Inputs.GoBack) }

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { vm.trySend(CounterContract.Inputs.GoBack) }) {
                            Icon(Icons.Default.ArrowBack, "")
                        }
                    },
                    title = { Text("BGG") },
                )
            },
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
                    Card {
                        Column(Modifier.padding(16.dp)) {
                            Text("Compose UI")
                            CounterComposeUi.Content(uiState) { vm.trySend(it) }
                        }
                    }
                    Card {
                        Column(Modifier.padding(16.dp)) {
                            var binding: FragmentCounterBinding? by remember { mutableStateOf(null) }
                            AndroidView(
                                factory = { context ->
                                    FragmentCounterBinding
                                        .inflate(LayoutInflater.from(context))
                                        .also { binding = it }
                                        .root
                                },
                                update = {
                                    binding?.apply {
                                        tvCounter.text = "${uiState.count}"

                                        btnDec.setOnClickListener { vm.trySend(CounterContract.Inputs.Decrement(1)) }
                                        btnInc.setOnClickListener { vm.trySend(CounterContract.Inputs.Increment(1)) }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        )
    }
}
