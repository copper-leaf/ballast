package com.copperleaf.ballast.test.internal

import com.copperleaf.ballast.test.TestResults
import com.copperleaf.ballast.test.ViewModelTestScenarioInputSequenceScope
import com.copperleaf.ballast.test.ViewModelTestScenarioScope
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class ViewModelTestScenarioScopeImpl<Inputs : Any, Events : Any, State : Any>(
    override val name: String
) : ViewModelTestScenarioScope<Inputs, Events, State> {
    internal var givenBlock: (() -> State)? = null
    internal lateinit var onInputSequenceBlock:
        suspend ViewModelTestScenarioInputSequenceScope<Inputs, Events, State>.() -> Unit
    internal lateinit var verifyBlock: TestResults<Inputs, Events, State>.() -> Unit
    internal var logger: ((String) -> Unit)? = null
    internal var timeout: Duration? = null

    override fun logger(block: (String) -> Unit) {
        logger = block
    }

    override fun timeout(timeout: Duration) {
        this.timeout = timeout
    }

    override fun given(block: () -> State) {
        givenBlock = block
    }

    override fun running(block: suspend ViewModelTestScenarioInputSequenceScope<Inputs, Events, State>.() -> Unit) {
        onInputSequenceBlock = block
    }

    override fun resultsIn(block: TestResults<Inputs, Events, State>.() -> Unit) {
        verifyBlock = block
    }
}
