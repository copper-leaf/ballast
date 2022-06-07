package com.copperleaf.ballast.examples.counter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.examples.util.ExamplesContext
import com.copperleaf.ballast.examples.util.LocalInjector
import com.copperleaf.ballast.examples.util.bulma.BulmaButton
import com.copperleaf.ballast.examples.util.bulma.BulmaButtonGroup
import com.copperleaf.ballast.examples.util.bulma.BulmaColor
import com.copperleaf.ballast.examples.util.bulma.BulmaPanel
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Text

object CounterWebUi {
    @Composable
    public fun WebContent() {
        val injector = LocalInjector.current

        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope) { injector.counterViewModel(viewModelCoroutineScope) }
        val uiState by vm.observeStates().collectAsState()

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
                    BulmaButton(onClick = { vm.trySend(CounterContract.Inputs.Decrement(1)) }) {
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
                    BulmaButton(onClick = { vm.trySend(CounterContract.Inputs.Increment(1)) }) {
                        Text("+")
                    }
                }
            }
        }
    }
}
