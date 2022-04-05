package com.copperleaf.ballast.impl

import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.EventHandlerScope

class TestEventHandler : EventHandler<
    TestContract.Inputs,
    TestContract.Events,
    TestContract.State> {
    override suspend fun EventHandlerScope<
        TestContract.Inputs,
        TestContract.Events,
        TestContract.State>.handleEvent(
        event: TestContract.Events
    ) = when (event) {
        is TestContract.Events.Notification -> {}
    }
}
