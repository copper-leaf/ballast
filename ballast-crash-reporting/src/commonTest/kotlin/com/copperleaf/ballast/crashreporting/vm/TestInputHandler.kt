package com.copperleaf.ballast.crashreporting.vm

import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputHandlerScope

class TestInputHandler : InputHandler<
        TestContract.Inputs,
        TestContract.Events,
        TestContract.State> {
    override suspend fun InputHandlerScope<
            TestContract.Inputs,
            TestContract.Events,
            TestContract.State>.handleInput(
        input: TestContract.Inputs
    ): Unit = when (input) {
        TestContract.Inputs.DontTrackThis -> {
            noOp()
        }
        TestContract.Inputs.TrackThis -> {
            noOp()
        }
    }
}
