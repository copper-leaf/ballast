package counter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import kotlin.time.ExperimentalTime

@ExperimentalTime
class CounterComponent(
    private val debuggerConnection: BallastDebuggerClientConnection<*>
) {
    @Composable
    fun Counter() {
        val coroutineScope = rememberCoroutineScope()
        val vm = remember(coroutineScope) { CounterViewModel(coroutineScope, debuggerConnection) }
        val vmState by vm.observeStates().collectAsState()

        LaunchedEffect(vm) {
            vm.send(CounterContract.Inputs.Initialize)
        }

        CounterUI(vmState) { vm.trySend(it) }
    }

    @Composable
    private fun CounterUI(
        uiState: CounterContract.State,
        postInput: (CounterContract.Inputs) -> Unit,
    ) {
        Div({ style { padding(25.px) } }) {
            Button(attrs = {
                onClick { postInput(CounterContract.Inputs.Decrement) }
            }) {
                Text("-")
            }

            Span({ style { padding(15.px) } }) {
                Text("${uiState.count}")
            }

            Button(attrs = {
                onClick { postInput(CounterContract.Inputs.Increment) }
            }) {
                Text("+")
            }
        }
    }
}
