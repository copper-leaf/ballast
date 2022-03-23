package com.copperleaf.ballast.test.internal

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.EventHandler
import com.copperleaf.ballast.InputFilter
import com.copperleaf.ballast.InputHandler
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.LifoInputStrategy
import com.copperleaf.ballast.test.ViewModelTestScenarioScope
import com.copperleaf.ballast.test.ViewModelTestSuiteScope
import com.copperleaf.ballast.test.internal.vm.TestInterceptorWrapper
import com.copperleaf.ballast.test.internal.vm.TestViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class ViewModelTestSuiteScopeImpl<Inputs : Any, Events : Any, State : Any>(
    internal val inputHandler: InputHandler<Inputs, Events, State>,
    internal val eventHandler: EventHandler<Inputs, Events, State>,
    internal val filter: InputFilter<Inputs, Events, State>?,
) : ViewModelTestSuiteScope<Inputs, Events, State> {

    internal var suiteLogger: BallastLogger = SimpleTestLogger()
    internal var defaultTimeout: Duration = 30.seconds
    internal var inputStrategy: InputStrategy = LifoInputStrategy()

    internal val interceptors: MutableList<() -> BallastInterceptor<TestViewModel.Inputs<Inputs>, Events, State>> =
        mutableListOf()

    internal var defaultInitialStateBlock: (() -> State)? = null
    internal val scenarioBlocks = mutableListOf<ViewModelTestScenarioScopeImpl<Inputs, Events, State>>()

    override fun logger(logger: BallastLogger) {
        this.suiteLogger = logger
    }

    override fun defaultTimeout(timeout: () -> Duration) {
        this.defaultTimeout = timeout()
    }

    override fun addInterceptor(interceptor: () -> BallastInterceptor<Inputs, Events, State>) {
        this.interceptors += { TestInterceptorWrapper(interceptor()) }
    }

    override fun defaultInputStrategy(inputStrategy: () -> InputStrategy) {
        this.inputStrategy = inputStrategy()
    }

    override fun defaultInitialState(block: () -> State) {
        defaultInitialStateBlock = block
    }

    override fun scenario(name: String, block: ViewModelTestScenarioScope<Inputs, Events, State>.() -> Unit) {
        scenarioBlocks += ViewModelTestScenarioScopeImpl<Inputs, Events, State>(name).apply(block)
    }
}
