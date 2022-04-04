package com.copperleaf.ballast.examples.web.counter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.examples.counter.CounterContract
import com.copperleaf.ballast.examples.util.ExamplesContext
import com.copperleaf.ballast.examples.web.util.Component
import com.copperleaf.ballast.examples.web.util.ComposeWebInjector
import com.copperleaf.ballast.examples.web.util.bulma.BulmaButton
import com.copperleaf.ballast.examples.web.util.bulma.BulmaButtonGroup
import com.copperleaf.ballast.examples.web.util.bulma.BulmaColor
import com.copperleaf.ballast.examples.web.util.bulma.BulmaPanel
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Text
import kotlin.time.ExperimentalTime

@ExperimentalTime
class CounterComponent(
    private val injector: ComposeWebInjector
) : Component {
    @Composable
    override fun Content() {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope) { injector.counterViewModel(viewModelCoroutineScope) }
        val vmState by vm.observeStates().collectAsState()

        CounterUI(vmState) { vm.trySend(it) }
    }

    @Composable
    private fun CounterUI(
        uiState: CounterContract.State,
        postInput: (CounterContract.Inputs) -> Unit,
    ) {
        BulmaPanel(
            headingStart = { Text("Counter") },
            headingEnd = {
                A(
                    href = "${ExamplesContext.samplesUrlWithVersion}/counter",
                    attrs = {
                        title("View sources on GitHub")
                        target(ATarget.Blank)
                    }
                ) {
                    I { Text("Sources") }
                }
            },
        ) {
            BulmaButtonGroup {
                Control {
                    BulmaButton(onClick = { postInput(CounterContract.Inputs.Decrement(1)) }) {
                        Text("-")
                    }
                }
                Control {
                    BulmaButton(
                        onClick = { },
                        color = BulmaColor.Default
                    ) {
                        Text("${uiState.count}")
                    }
                }
                Control {
                    BulmaButton(onClick = { postInput(CounterContract.Inputs.Increment(1)) }) {
                        Text("+")
                    }
                }
            }
        }
    }
}
