package com.copperleaf.ballast.test.internal

import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.test.BallastIsolatedScenarioScope
import com.copperleaf.ballast.test.TestResults
import kotlin.time.Duration

internal class BallastIsolatedScenarioScopeImpl<Inputs : Any, Events : Any, State : Any>(
    internal val delegate: BallastScenarioScopeImpl<Inputs, Events, State>
) : BallastIsolatedScenarioScope<Inputs, Events, State> {
    override val name: String
        get() = delegate.name

    override fun solo() {
        delegate.solo()
    }

    override fun skip() {
        delegate.skip()
    }

    override fun logger(logger: (String) -> BallastLogger) {
        delegate.logger(logger)
    }

    override fun timeout(timeout: () -> Duration) {
        delegate.timeout(timeout)
    }

    override fun given(block: () -> State) {
        delegate.given(block)
    }

    override fun resultsIn(block: TestResults<Inputs, Events, State>.() -> Unit) {
        delegate.resultsIn(block)
    }
}
