package com.copperleaf.ballast.test

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.LoggingInterceptor
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
public interface ViewModelTestSuiteScope<Inputs : Any, Events : Any, State : Any> {

    /**
     * A callback function for viewing logs emitted during this test suite. This includes logs from a
     * [LoggingInterceptor], and additional logs from this test runner.
     */
    public fun logger(block: (String) -> Unit)

    /**
     * Set the default timeout for waiting for test side-effects to complete.
     */
    public fun defaultTimeout(timeout: () -> Duration)

    /**
     * Adds the Interceptor to all tests in this scenario.
     */
    public fun addInterceptor(interceptor: () -> BallastInterceptor<Inputs, Events, State>)

    /**
     * Set the default input strategy to use for this test suite. Each scenario may override its own strategy.
     */
    public fun defaultInputStrategy(inputStrategy: () -> InputStrategy)

    /**
     * Provide a default initial State used for running all tests in this suite. Each scenario may override this initial
     * state with their own [ViewModelTestScenarioScope.given] block
     */
    public fun defaultInitialState(block: () -> State)

    /**
     * Add a scenario to this ViewModel test suite.
     */
    public fun scenario(name: String, block: ViewModelTestScenarioScope<Inputs, Events, State>.() -> Unit)
}
