package com.copperleaf.ballast.test

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.InputStrategy
import com.copperleaf.ballast.core.LoggingInterceptor
import kotlin.time.Duration

public interface BallastScenarioScope<Inputs : Any, Events : Any, State : Any> {
    public val name: String

    /**
     * Mark this as the only scenario that should be run in the entire suite. Useful for troubleshooting a single
     * failing test in a large suite without having to comment out the other tests.
     */
    public fun solo()

    /**
     * Do not run this test in the suite.
     */
    public fun skip()

    /**
     * A callback function for viewing logs emitted during this test scenario. This includes logs from a
     * [LoggingInterceptor], and additional logs from this test runner.
     */
    public fun logger(logger: (String)->BallastLogger)

    /**
     * Set the timeout for waiting for test side-jobs to complete for this test scenario.
     */
    public fun timeout(timeout: () -> Duration)

    /**
     * Set the input strategy to use for this test.
     */
    public fun inputStrategy(inputStrategy: () -> InputStrategy<Inputs, Events, State>)

    /**
     * Adds the Interceptor to this scenario
     */
    public fun addInterceptor(interceptor: () -> BallastInterceptor<Inputs, Events, State>)

    /**
     * Provide an alternative starting state for this scenario. Overrides the default starting state provided to the
     * entire suite.
     */
    public fun given(block: () -> State)

    /**
     * The input sequence that this scenario will execute. Inputs are processed in order, and the block will suspend for
     * one input to actually be accepted and be processed by the ViewModel before starting to process the next.
     *
     * This entire script will run to completion and all sent inputs will be handled before final test results are
     * collected and sent to [BallastScenarioScope.resultsIn] for verification.
     */
    public fun running(block: suspend BallastScenarioInputSequenceScope<Inputs, Events, State>.() -> Unit)

    /**
     * Once the scneario test script in [BallastScenarioScope.running] has completed, inspect and make assertions
     * on what actually happened during the test, and what it produced as a result. The properties in [TestResults]
     * correspond directly to the callbacks of [BallastInterceptor], and the relative ordering of properties in each
     * list is maintained with respect to the order the inputs were delivered to the test.
     */
    public fun resultsIn(block: TestResults<Inputs, Events, State>.() -> Unit)
}
