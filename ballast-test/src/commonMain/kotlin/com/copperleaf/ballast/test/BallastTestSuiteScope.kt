package com.copperleaf.ballast.test

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.LoggingInterceptor
import kotlin.time.Duration

public interface BallastTestSuiteScope<Inputs : Any, Events : Any, State : Any> {

    /**
     * Do not run this test suite.
     */
    public fun skip()

    /**
     * A callback function for viewing logs emitted during this test suite. This includes logs from a
     * [LoggingInterceptor], and additional logs from this test runner.
     */
    public fun logger(logger: (String)->BallastLogger)

    /**
     * Set the default timeout for waiting for test side-jobs to complete.
     */
    public fun defaultTimeout(timeout: () -> Duration)

    /**
     * Adds the Interceptor to all tests in this scenario.
     */
    public fun addInterceptor(interceptor: () -> BallastInterceptor<Inputs, Events, State>)

    /**
     * Set the default input strategy to use for this test suite. Each scenario may override its own strategy.
     */
    public fun defaultInputStrategy(inputStrategy: () -> InputStrategy<Inputs, Events, State>)

    /**
     * Provide a default initial State used for running all tests in this suite. Each scenario may override this initial
     * state with their own [BallastScenarioScope.given] block
     */
    public fun defaultInitialState(block: () -> State)

    /**
     * Add a scenario to this ViewModel test suite.
     */
    public fun scenario(name: String, block: BallastScenarioScope<Inputs, Events, State>.() -> Unit)

    /**
     * Isolate a single Input
     */
    public fun isolatedScenario(input: Inputs, name: String = input.toString(), block: BallastIsolatedScenarioScope<Inputs, Events, State>.() -> Unit)
}
