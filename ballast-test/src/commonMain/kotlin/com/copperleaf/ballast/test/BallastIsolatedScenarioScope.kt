package com.copperleaf.ballast.test

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastLogger
import com.copperleaf.ballast.core.LoggingInterceptor
import kotlin.time.Duration

public interface BallastIsolatedScenarioScope<Inputs : Any, Events : Any, State : Any> {
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
     * Provide an alternative starting state for this scenario. Overrides the default starting state provided to the
     * entire suite.
     */
    public fun given(block: () -> State)

    /**
     * Once the scneario test script in [BallastIsolatedScenarioScope.running] has completed, inspect and make assertions
     * on what actually happened during the test, and what it produced as a result. The properties in [TestResults]
     * correspond directly to the callbacks of [BallastInterceptor], and the relative ordering of properties in each
     * list is maintained with respect to the order the inputs were delivered to the test.
     */
    public fun resultsIn(block: TestResults<Inputs, Events, State>.() -> Unit)
}
