package com.copperleaf.ballast.examples.counter

import androidx.compose.runtime.Composable
import com.copperleaf.ballast.examples.util.ExamplesContext
import com.copperleaf.ballast.examples.util.bulma.BulmaButton
import com.copperleaf.ballast.examples.util.bulma.BulmaButtonGroup
import com.copperleaf.ballast.examples.util.bulma.BulmaColor
import com.copperleaf.ballast.examples.util.bulma.BulmaPanel
import org.jetbrains.compose.web.attributes.ATarget
import org.jetbrains.compose.web.attributes.target
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Text

object CounterSyncWebUi {
    @Composable
    public fun Content(
        uiState: CounterContract.State,
        postInput: (CounterContract.Inputs) -> Unit,
    ) {
        BulmaPanel(
            headingStart = { Text("Sync") },
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
