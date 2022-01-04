package com.copperleaf.ballast.test

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
    public fun defaultTimeout(timeout: Duration)

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
