package com.copperleaf.ballast.examples.kitchensink.controller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkWebUi
import com.copperleaf.ballast.examples.util.ExamplesContext
import com.copperleaf.ballast.examples.util.LocalInjector
import com.copperleaf.ballast.examples.util.bulma.BulmaPanel
import com.copperleaf.ballast.examples.util.bulma.BulmaSelect
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Text

object KitchenSinkControllerWebUi {
    @Composable
    public fun WebContent() {
        val injector = LocalInjector.current

        val viewModelCoroutineScope = rememberCoroutineScope()
        val controllerVm = remember(viewModelCoroutineScope) {
            injector.kitchenSinkControllerViewModel(viewModelCoroutineScope)
        }
        val controllerUiState by controllerVm.observeStates().collectAsState()

        BulmaPanel(
            headingStart = { Text("Kitchen Sink") },
            headingEnd = {
                A(
                    href = "${ExamplesContext.samplesUrlWithVersion}/kitchensink",
                    attrs = {
                        title("View sources on GitHub")
                        target(ATarget.Blank)
                    }
                ) {
                    I { Text("Sources") }
                }
            },
        ) {
            BulmaSelect(
                fieldName = "Input Strategy",
                items = InputStrategySelection.values().toList(),
                itemValue = { it.name },
                selectedValue = controllerUiState.inputStrategy,
                onValueChange = { controllerVm.trySend(KitchenSinkControllerContract.Inputs.UpdateInputStrategy(it)) },
                itemContent = { Text(it.name) }
            )

            if (controllerUiState.viewModel != null) {
                val sampleUiState by controllerUiState.viewModel!!.observeStates().collectAsState()

                KitchenSinkWebUi.Content(
                    uiState = sampleUiState,
                    postInput = { controllerUiState.viewModel!!.trySend(it) },
                )
            }
        }
    }
}
