package com.copperleaf.ballast.examples.ui.counter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.examples.injector.ComposeWebInjector
import com.copperleaf.ballast.examples.ui.util.bulma.BulmaButton
import com.copperleaf.ballast.examples.ui.util.bulma.BulmaButtonGroup
import com.copperleaf.ballast.examples.ui.util.bulma.BulmaColor
import org.jetbrains.compose.web.dom.Text

object CounterWebUi {

    @Composable
    fun Content(injector: ComposeWebInjector) {
        val viewModelCoroutineScope = rememberCoroutineScope()
        val vm = remember(viewModelCoroutineScope) { injector.counterViewModel(viewModelCoroutineScope, null, null) }
        val uiState by vm.observeStates().collectAsState()

        Content(uiState) { vm.trySend(it) }
    }

    @Composable
    public fun Content(
        uiState: CounterContract.State,
        postInput: (CounterContract.Inputs) -> Unit,
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
