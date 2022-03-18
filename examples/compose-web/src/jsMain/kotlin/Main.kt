
import androidx.compose.runtime.LaunchedEffect
import com.copperleaf.ballast.debugger.BallastDebuggerClientConnection
import counter.CounterComponent
import io.ktor.client.engine.js.Js
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.jetbrains.compose.web.renderComposable
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun main() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val connection = BallastDebuggerClientConnection(Js, applicationScope) {
        this.engine {

        }
    }
    val counterComponent = CounterComponent(connection)

    renderComposable(rootElementId = "root") {
        LaunchedEffect(Unit) { connection.connect() }

        counterComponent.Counter()
    }
}

