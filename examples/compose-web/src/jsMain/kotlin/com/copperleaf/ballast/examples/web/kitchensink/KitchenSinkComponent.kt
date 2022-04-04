package com.copperleaf.ballast.examples.web.kitchensink

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.core.LifoInputStrategy
import com.copperleaf.ballast.examples.kitchensink.KitchenSinkContract
import com.copperleaf.ballast.examples.util.ExamplesContext
import com.copperleaf.ballast.examples.web.util.Component
import com.copperleaf.ballast.examples.web.util.ComposeWebInjector
import com.copperleaf.ballast.examples.web.util.bulma.BulmaButton
import com.copperleaf.ballast.examples.web.util.bulma.BulmaPanel
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Progress
import org.jetbrains.compose.web.dom.Text
import kotlin.time.ExperimentalTime

@ExperimentalTime
class KitchenSinkComponent(
    private val injector: ComposeWebInjector
) : Component {
    @Composable
    override fun Content() {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope) {
            injector.kitchenSinkViewModel(
                viewModelCoroutineScope,
                LifoInputStrategy()
            )
        }
        val vmState by vm.observeStates().collectAsState()

        CounterUI(vmState) { vm.trySend(it) }
    }

    @Composable
    private fun CounterUI(
        uiState: KitchenSinkContract.State,
        postInput: (KitchenSinkContract.Inputs) -> Unit,
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
            Text("State")
            Hr { }
            Text("${uiState.infiniteCounter}")
            if (uiState.loading) {
                Progress { }
            }

            Text("Actions")
            Hr { }

            Text("Inputs")
            BulmaButton(
                onClick = { postInput(KitchenSinkContract.Inputs.LongRunningInput()) },
            ) {
                Text("LongRunningInput")
            }
            BulmaButton(
                onClick = { postInput(KitchenSinkContract.Inputs.ErrorRunningInput) },
            ) {
                Text("ErrorRunningInput")
            }

            Text("Events")
            BulmaButton(
                onClick = { postInput(KitchenSinkContract.Inputs.LongRunningEvent) },
            ) {
                Text("LongRunningEvent")
            }
            BulmaButton(
                onClick = { postInput(KitchenSinkContract.Inputs.ErrorRunningEvent) },
            ) {
                Text("ErrorRunningEvent")
            }
            BulmaButton(
                onClick = { postInput(KitchenSinkContract.Inputs.CloseKitchenSinkWindow) },
            ) {
                Text("CloseKitchenSinkWindow")
            }

            Text("SideJobs")
            BulmaButton(
                onClick = { postInput(KitchenSinkContract.Inputs.LongRunningSideJob) },
            ) {
                Text("LongRunningSideJob")
            }
            BulmaButton(
                onClick = { postInput(KitchenSinkContract.Inputs.ErrorRunningSideJob) },
            ) {
                Text("ErrorRunningSideJob")
            }
            BulmaButton(
                onClick = { postInput(KitchenSinkContract.Inputs.InfiniteSideJob) },
            ) {
                Text("InfiniteSideJob")
            }
            BulmaButton(
                onClick = { postInput(KitchenSinkContract.Inputs.CancelInfiniteSideJob) },
            ) {
                Text("CancelInfiniteSideJob")
            }
        }
    }
}
