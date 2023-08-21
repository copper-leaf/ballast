package com.copperleaf.ballast.test.internal

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.BallastViewModelConfiguration
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.test.BallastScenarioInputSequenceScope
import com.copperleaf.ballast.test.BallastScenarioScope
import com.copperleaf.ballast.test.TestResults
import kotlin.time.Duration

internal class BallastScenarioScopeImpl<Inputs : Any, Events : Any, State : Any>(
    override val name: String
) : BallastScenarioScope<Inputs, Events, State> {
    internal var givenBlock: (() -> State)? = null
    internal var configurationBlock: ((BallastViewModelConfiguration.TypedBuilder<Inputs, Events, State>) -> BallastViewModelConfiguration.TypedBuilder<Inputs, Events, State>)? = null
    internal lateinit var onInputSequenceBlock:
            suspend BallastScenarioInputSequenceScope<Inputs, Events, State>.() -> Unit
    internal lateinit var verifyBlock: TestResults<Inputs, Events, State>.() -> Unit

    internal var solo: Boolean = false
    internal var skip: Boolean = false

    internal var logger: ((String) -> BallastLogger)? = null
    internal var timeout: Duration? = null
    internal var inputStrategy: () -> InputStrategy<Inputs, Events, State>? = { null }

    internal val interceptors: MutableList<() -> BallastInterceptor<Inputs, Events, State>> =
        mutableListOf()

    override fun solo() {
        this.solo = true
    }

    override fun skip() {
        this.skip = true
    }

    override fun logger(logger: (String) -> BallastLogger) {
        this.logger = logger
    }

    override fun timeout(timeout: () -> Duration) {
        this.timeout = timeout()
    }

    override fun inputStrategy(inputStrategy: () -> InputStrategy<Inputs, Events, State>) {
        this.inputStrategy = inputStrategy
    }

    override fun addInterceptor(interceptor: () -> BallastInterceptor<Inputs, Events, State>) {
        this.interceptors += interceptor
    }

    override fun given(block: () -> State) {
        givenBlock = block
    }

    override fun running(block: suspend BallastScenarioInputSequenceScope<Inputs, Events, State>.() -> Unit) {
        onInputSequenceBlock = block
    }

    override fun resultsIn(block: TestResults<Inputs, Events, State>.() -> Unit) {
        verifyBlock = block
    }

    override fun customizeConfiguration(configure: (BallastViewModelConfiguration.TypedBuilder<Inputs, Events, State>) -> BallastViewModelConfiguration.TypedBuilder<Inputs, Events, State>) {
        configurationBlock = configure
    }
}
