package com.copperleaf.ballast.test.internal

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.test.TestResults
import com.copperleaf.ballast.test.ViewModelTestScenarioInputSequenceScope
import com.copperleaf.ballast.test.ViewModelTestScenarioScope
import com.copperleaf.ballast.test.internal.vm.TestInterceptorWrapper
import com.copperleaf.ballast.test.internal.vm.TestViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
internal class ViewModelTestScenarioScopeImpl<Inputs : Any, Events : Any, State : Any>(
    override val name: String
) : ViewModelTestScenarioScope<Inputs, Events, State> {
    internal var givenBlock: (() -> State)? = null
    internal lateinit var onInputSequenceBlock:
        suspend ViewModelTestScenarioInputSequenceScope<Inputs, Events, State>.() -> Unit
    internal lateinit var verifyBlock: TestResults<Inputs, Events, State>.() -> Unit

    internal var solo: Boolean = false
    internal var skip: Boolean = false

    internal var logger: ((String) -> Unit)? = null
    internal var timeout: Duration? = null
    internal var inputStrategy: InputStrategy? = null

    internal val interceptors: MutableList<()->BallastInterceptor<TestViewModel.Inputs<Inputs>, Events, State>> =
        mutableListOf()

    override fun solo() {
        this.solo = true
    }

    override fun skip() {
        this.skip = true
    }

    override fun logger(block: (String) -> Unit) {
        this.logger = block
    }

    override fun timeout(timeout: () -> Duration) {
        this.timeout = timeout()
    }

    override fun inputStrategy(inputStrategy: () -> InputStrategy) {
        this.inputStrategy = inputStrategy()
    }

    override fun addInterceptor(interceptor: () -> BallastInterceptor<Inputs, Events, State>) {
        this.interceptors += { TestInterceptorWrapper(interceptor()) }
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
