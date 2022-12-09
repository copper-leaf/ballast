package com.copperleaf.ballast.examples.ui.kitchensink

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.examples.injector.ComposeWebInjector
import com.copperleaf.ballast.examples.ui.util.bulma.BulmaButton
import com.copperleaf.ballast.examples.ui.util.bulma.BulmaSelect
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Progress
import org.jetbrains.compose.web.dom.Text

object KitchenSinkWebUi {

    @Composable
    fun Content(injector: ComposeWebInjector, inputStrategySelection: InputStrategySelection) {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope, inputStrategySelection) {
            injector.kitchenSinkViewModel(
                viewModelCoroutineScope,
                inputStrategySelection
            )
        }
        val uiState by vm.observeStates().collectAsState()

        Content(uiState) { vm.trySend(it) }
    }

    @Composable
    public fun Content(
        uiState: KitchenSinkContract.State,
        postInput: (KitchenSinkContract.Inputs) -> Unit,
    ) {
        BulmaSelect(
            fieldName = "Input Strategy",
            items = InputStrategySelection.values().toList(),
            itemValue = { it.name },
            selectedValue = uiState.inputStrategy,
            onValueChange = { postInput(KitchenSinkContract.Inputs.ChangeInputStrategy(it)) },
            itemContent = { Text(it.name) }
        )

        Text("State")
        Hr { }
        Text("Completed Input: ${uiState.completedInputCounter}")
        Br { }
        Text("Counter: ${uiState.infiniteCounter}")
        Br { }
        if (uiState.loading) {
            Progress { }
        }

        Text("Actions")
        Hr { }

        Text("Inputs")
        Br { }
        BulmaButton(
            onClick = { postInput(KitchenSinkContract.Inputs.LongRunningInput()) },
        ) {
            Text("LongRunningInput")
        }
        Br { }
        BulmaButton(
            onClick = { postInput(KitchenSinkContract.Inputs.ErrorRunningInput) },
        ) {
            Text("ErrorRunningInput")
        }
        Br { }

        Text("Events")
        Br { }
        BulmaButton(
            onClick = { postInput(KitchenSinkContract.Inputs.LongRunningEvent) },
        ) {
            Text("LongRunningEvent")
        }
        Br { }
        BulmaButton(
            onClick = { postInput(KitchenSinkContract.Inputs.ErrorRunningEvent) },
        ) {
            Text("ErrorRunningEvent")
        }
        Br { }
        BulmaButton(
            onClick = { postInput(KitchenSinkContract.Inputs.CloseKitchenSinkWindow) },
        ) {
            Text("CloseKitchenSinkWindow")
        }
        Br { }

        Text("SideJobs")
        Br { }
        BulmaButton(
            onClick = { postInput(KitchenSinkContract.Inputs.LongRunningSideJob) },
        ) {
            Text("LongRunningSideJob")
        }
        Br { }
        BulmaButton(
            onClick = { postInput(KitchenSinkContract.Inputs.ErrorRunningSideJob) },
        ) {
            Text("ErrorRunningSideJob")
        }
        Br { }
        BulmaButton(
            onClick = { postInput(KitchenSinkContract.Inputs.InfiniteSideJob) },
        ) {
            Text("InfiniteSideJob")
        }
        Br { }
        BulmaButton(
            onClick = { postInput(KitchenSinkContract.Inputs.CancelInfiniteSideJob) },
        ) {
            Text("CancelInfiniteSideJob")
        }
        Br { }
    }
}
