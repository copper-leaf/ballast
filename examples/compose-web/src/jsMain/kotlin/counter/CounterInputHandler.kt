package counter

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope

class CounterInputHandler : InputHandler<
    CounterContract.Inputs,
    CounterContract.Events,
    CounterContract.State> {
    override suspend fun InputHandlerScope<
        CounterContract.Inputs,
        CounterContract.Events,
        CounterContract.State>.handleInput(
        input: CounterContract.Inputs
    ) = when (input) {
        CounterContract.Inputs.Initialize -> {
            noOp()
        }
        CounterContract.Inputs.Increment -> {
            updateState { it.copy(count = it.count + 1) }
        }
        CounterContract.Inputs.Decrement -> {
            updateState { it.copy(count = it.count - 1) }
        }
    }
}
