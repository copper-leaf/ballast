package com.copperleaf.ballast.examples.kitchensink.controller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkWebUi
import com.copperleaf.ballast.examples.util.ExamplesContext
import com.copperleaf.ballast.examples.util.bulma.BulmaPanel
import com.copperleaf.ballast.examples.util.bulma.BulmaSelect
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Text

object KitchenSinkControllerWebUi {
    @Composable
    public fun Content(
        uiState: KitchenSinkControllerContract.State,
        postInput: (KitchenSinkControllerContract.Inputs) -> Unit,
    ) {
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
                selectedValue = uiState.inputStrategy,
                onValueChange = { postInput(KitchenSinkControllerContract.Inputs.UpdateInputStrategy(it)) },
                itemContent = { Text(it.name) }
            )

            if (uiState.viewModel != null) {
                val sampleUiState by uiState.viewModel.observeStates().collectAsState()

                KitchenSinkWebUi.Content(
                    uiState = sampleUiState,
                    postInput = { uiState.viewModel.trySend(it) },
                )
            }
        }
    }
}
