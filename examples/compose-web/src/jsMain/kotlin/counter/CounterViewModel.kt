package counter

import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.core.BasicViewModel
import com.copperleaf.ballast.core.LoggingInterceptor
import com.copperleaf.ballast.core.PrintlnLogger
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import com.copperleaf.ballast.debugger.BallastDebuggerInterceptor
import com.copperleaf.ballast.forViewModel
import com.copperleaf.ballast.plusAssign
import kotlinx.coroutines.CoroutineScope
import kotlin.time.ExperimentalTime

@ExperimentalTime
class CounterViewModel(
    coroutineScope: CoroutineScope,
    debuggerConnection: BallastDebuggerClientConnection<*>
) : BasicViewModel<
    CounterContract.Inputs,
    CounterContract.Events,
    CounterContract.State>(
    config = BallastViewModelConfiguration.Builder()
        .apply {
            this += BallastDebuggerInterceptor(debuggerConnection)
            this += LoggingInterceptor()
            logger = PrintlnLogger()
        }
        .forViewModel(
            initialState = CounterContract.State(),
            inputHandler = CounterInputHandler(),
            name = "Counter",
        ),
    eventHandler = CounterEventHandler(),
    coroutineScope = coroutineScope,
)
