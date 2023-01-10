package com.copperleaf.ballast.test.internal

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.InputFilter
import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.LifoInputStrategy
import com.copperleaf.ballast.test.BallastIsolatedScenarioScope
import com.copperleaf.ballast.test.BallastScenarioScope
import com.copperleaf.ballast.test.BallastTestSuiteScope
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class BallastTestSuiteScopeImpl<Inputs : Any, Events : Any, State : Any>(
    internal val inputHandler: InputHandler<Inputs, Events, State>,
    internal val eventHandler: EventHandler<Inputs, Events, State>,
    internal val filter: InputFilter<Inputs, Events, State>?,
) : BallastTestSuiteScope<Inputs, Events, State> {

    internal var skip: Boolean = false

    internal var suiteLogger: (String) -> BallastLogger = { SimpleTestLogger() }
    internal var defaultTimeout: Duration = 30.seconds
    internal var inputStrategy: InputStrategy<Inputs, Events, State> = LifoInputStrategy.typed()

    internal val interceptors: MutableList<() -> BallastInterceptor<Inputs, Events, State>> =
        mutableListOf()

    internal var defaultInitialStateBlock: (() -> State)? = null
    internal val scenarioBlocks = mutableListOf<BallastScenarioScopeImpl<Inputs, Events, State>>()

    override fun skip() {
        this.skip = true
    }

    override fun logger(logger: (String) -> BallastLogger) {
        this.suiteLogger = logger
    }

    override fun defaultTimeout(timeout: () -> Duration) {
        this.defaultTimeout = timeout()
    }

    override fun addInterceptor(interceptor: () -> BallastInterceptor<Inputs, Events, State>) {
        this.interceptors += interceptor
    }

    override fun defaultInputStrategy(inputStrategy: () -> InputStrategy<Inputs, Events, State>) {
        this.inputStrategy = inputStrategy()
    }

    override fun defaultInitialState(block: () -> State) {
        defaultInitialStateBlock = block
    }

    override fun scenario(name: String, block: BallastScenarioScope<Inputs, Events, State>.() -> Unit) {
        scenarioBlocks += BallastScenarioScopeImpl<Inputs, Events, State>(name).apply(block)
    }

    override fun isolatedScenario(
        input: Inputs,
        name: String,
        block: BallastIsolatedScenarioScope<Inputs, Events, State>.() -> Unit
    ) {
        scenarioBlocks += BallastIsolatedScenarioScopeImpl(
            delegate = BallastScenarioScopeImpl<Inputs, Events, State>(name).apply {
                running {
                    +input
                }
            }
        )
            .apply(block)
            .delegate
    }
}
